package com.fitness.service;
import com.fitness.dto.OrderRequest;
import com.fitness.dto.OrderResponse;
import com.fitness.dto.OrderItemRequest;
import com.fitness.dto.OrderSummaryResponse;
import com.fitness.model.Order;
import com.fitness.model.OrderItem;
import com.fitness.model.User;
import com.fitness.model.Product;
import com.fitness.repository.OrderRepository;
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

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getMyOrders(User user){
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    private OrderSummaryResponse  mapToSummary(Order order){
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderStatus().name(),
                order.getTotalAmount(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }

    @Transactional(readOnly= true)
    public OrderResponse getMyOrderById(Long orderId, User user){
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND
                , "Order not found with id: " +orderId));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest req, User user){
        Map<Long, Integer> product_qty = mergeItems(req.items());
        Map<Long, Product> productMap = productRepository
                .findAllByIdForUpdate(product_qty.keySet())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(req.receiverName().trim());
        order.setPhone(req.phone().trim());
        order.setShippingAddress(req.shippingAddress().trim());
        order.setNote(req.note() == null ? null : req.note().trim());
        order.setOrderStatus(Order.OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : product_qty.entrySet()) {
            Long  productId = entry.getKey();
            Integer quantity = entry.getValue();
            Product product = productMap.get(productId);
            if  (product == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product not found with id:" + productId);
            }

            validateProduct(product, quantity);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubTotal(subTotal);

            order.addItem(orderItem);
            product.decreaseStock(quantity);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    private Map<Long, Integer> mergeItems(List<OrderItemRequest> items){
        if (items == null || items.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order must contain at least one item");
        }

        Map<Long, Integer> productQty = new LinkedHashMap<>();
        for (OrderItemRequest item : items){
            if (item == null || item.productId() == null
            || item.quantity() == null || item.quantity() <= 0){
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
            if (qty > MAX_QTY_PER_PRODUCT){
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

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item ->
                        new OrderResponse.OrderItemResponse(
                                item.getProduct().getId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubTotal()
                        )).toList();

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
                items
        );
    }
}









