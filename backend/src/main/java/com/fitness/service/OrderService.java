package com.fitness.service;

import com.fitness.config.PaymentProperties;
import com.fitness.dto.OrderItemRequest;
import com.fitness.dto.OrderRequest;
import com.fitness.dto.OrderResponse;
import com.fitness.dto.OrderSummaryResponse;
import com.fitness.dto.PaymentInfoResponse;
import com.fitness.dto.ValidateVoucherResponse;
import com.fitness.model.Order;
import com.fitness.model.OrderItem;
import com.fitness.model.Product;
import com.fitness.model.User;
import com.fitness.repository.OrderRepository;
import com.fitness.repository.PaymentRepository;
import com.fitness.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final int MAX_QTY_PER_PRODUCT = 50;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherService voucherService;
    private final SePayPaymentService sePayPaymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentProperties paymentProperties;

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getMyOrders(User user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    private OrderSummaryResponse mapToSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderStatus().name(),
                order.getTotalAmount(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(Long orderId, User user) {
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with id: " + orderId));
        PaymentInfoResponse paymentInfo = paymentRepository
                .findTopByRefTypeAndRefIdOrderByCreatedAtDesc("ORDER", order.getId())
                .map(p -> PaymentInfoResponse.from(p, paymentProperties.getSepay().getBankName()))
                .orElse(null);
        return mapToResponse(order, paymentInfo);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest req, User user) {
        Map<Long, Integer> productQty = mergeItems(req.items());
        Map<Long, Product> productMap = productRepository
                .findAllByIdForUpdate(productQty.keySet())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(req.receiverName().trim());
        order.setPhone(req.phone().trim());
        order.setShippingAddress(req.shippingAddress().trim());
        order.setNote(req.note() == null ? null : req.note().trim());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : productQty.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            Product product = productMap.get(productId);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product not found with id:" + productId);
            }

            validateProduct(product, quantity);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemSubTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(itemSubTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubTotal(itemSubTotal);

            order.addItem(orderItem);
            product.decreaseStock(quantity);
        }

        // Tính giảm giá & trừ lượt Voucher tại Server (không tin số tiền từ Client)
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;
        ValidateVoucherResponse voucherRes = voucherService.applyAndConsumeVoucher(
                req.voucherCode(), subtotal, "PRODUCT");
        if (voucherRes != null) {
            discountAmount = voucherRes.discountAmount();
            appliedVoucherCode = voucherRes.voucher().code();
        }
        BigDecimal finalTotal = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        String rawMethod = (req.paymentMethod() != null && !req.paymentMethod().isBlank())
                ? req.paymentMethod().trim().toUpperCase(Locale.ROOT)
                : "COD";
        String paymentMethod = ("VIETQR".equals(rawMethod) || "TRANSFER".equals(rawMethod)) ? "VIETQR" : "COD";
        boolean isFree = finalTotal.compareTo(BigDecimal.ZERO) == 0;
        boolean isVietQrPending = "VIETQR".equals(paymentMethod) && !isFree;

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setVoucherCode(appliedVoucherCode);
        order.setTotalAmount(finalTotal);
        order.setPaymentMethod(paymentMethod);

        if (isFree) {
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        } else if (isVietQrPending) {
            order.setOrderStatus(Order.OrderStatus.PENDING);
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
        } else {
            order.setOrderStatus(Order.OrderStatus.PENDING);
            order.setPaymentStatus(Order.PaymentStatus.UNPAID);
        }

        Order savedOrder = orderRepository.save(order);

        PaymentInfoResponse paymentInfo = null;
        if (isVietQrPending) {
            paymentInfo = sePayPaymentService.createVietQrPayment(
                    user, "ORDER", savedOrder.getId(), finalTotal, null);
        } else if (isFree) {
            sePayPaymentService.createImmediatePaidPayment(
                    user, "ORDER", savedOrder.getId(), BigDecimal.ZERO, paymentMethod, null);
        }

        return mapToResponse(savedOrder, paymentInfo);
    }

    private Map<Long, Integer> mergeItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order must contain at least one item");
        }

        Map<Long, Integer> productQty = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            if (item == null || item.productId() == null
                    || item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product Quantity must > 0");
            }

            try {
                productQty.merge(item.productId(), item.quantity(), Math::addExact);
            } catch (ArithmeticException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Total quantity is too large");
            }
        }

        productQty.forEach((id, qty) -> {
            if (qty > MAX_QTY_PER_PRODUCT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quantity per product mus not exceed " + MAX_QTY_PER_PRODUCT);
            }
        });
        return productQty;
    }

    private void validateProduct(Product product, int quantity) {
        if (product.getProductStatus() == Product.ProductStatus.OUT_OF_STOCK) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Out of stock: " + product.getName());
        }
        if (product.getProductStatus() != Product.ProductStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product is not available: " + product.getName());
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Not enough stock for: " + product.getName());
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid product price: " + product.getName());
        }
    }

    private OrderResponse mapToResponse(Order order, PaymentInfoResponse paymentInfo) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubTotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getFullName(),
                order.getReceiverName(),
                order.getPhone(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getNote(),
                order.getOrderStatus().name(),
                order.getCreatedAt(),
                items,
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getVoucherCode(),
                order.getPaymentMethod(),
                order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null,
                paymentInfo
        );
    }
}
