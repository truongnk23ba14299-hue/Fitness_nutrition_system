package com.fitness.service;

import com.fitness.Exception.ResourceNotFoundException;
import com.fitness.dto.AdminOrderResponse;
import com.fitness.model.Order;
import com.fitness.model.Payment;
import com.fitness.repository.OrderRepository;
import com.fitness.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SePayPaymentService sePayPaymentService;
    private final VoucherService voucherService;

    // ==== Minh Quang: Admin quản lý xử lý đơn hàng của học viên ====
    // Dùng chung model Order của tuantinhte17-droid (confirm/ship/deliver/complete/cancel)

    // Xem toàn bộ đơn hàng trong phòng gym (lọc theo PENDING, COMPLETED,...)
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getAllOrders(Order.OrderStatus status) {
        List<Order> orders = (status == null)
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByOrderStatusOrderByCreatedAtDesc(status);

        return orders.stream()
                .map(AdminOrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderById(Long id) {
        return AdminOrderResponse.fromEntity(findOrThrow(id));
    }

    // Duyệt đơn hàng: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED / CANCELLED
    // Đi qua các domain method có sẵn của Order (confirm/ship/deliver/complete/cancel)
    // cancel() sẽ tự hoàn lại tồn kho cho từng sản phẩm trong đơn
    @Transactional
    public AdminOrderResponse updateOrderStatus(Long id, Order.OrderStatus target) {
        Order order = findOrThrow(id);

        if (order.getOrderStatus() == target) {
            throw new IllegalArgumentException("Đơn hàng đã ở trạng thái " + target);
        }

        try {
            switch (target) {
                case CONFIRMED -> order.confirm();
                case SHIPPING -> order.ship();
                case DELIVERED -> order.deliver();
                case COMPLETED -> order.complete();
                case CANCELLED -> order.cancel();
                case PENDING -> throw new IllegalArgumentException("Không thể chuyển đơn hàng về PENDING");
            }
        } catch (IllegalStateException e) {
            // Trả 400 thay vì 500 khi luồng chuyển trạng thái không hợp lệ
            throw new IllegalArgumentException(e.getMessage());
        }

        if (target == Order.OrderStatus.COMPLETED && order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            var existingPaymentOpt = paymentRepository.findTopByRefTypeAndRefIdOrderByCreatedAtDesc("ORDER", order.getId());
            if (existingPaymentOpt.isPresent()) {
                Payment payment = existingPaymentOpt.get();
                if (!"PAID".equals(payment.getStatus())) {
                    payment.setStatus("PAID");
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                }
            } else {
                String method = order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD";
                sePayPaymentService.createImmediatePaidPayment(
                        order.getUser(), "ORDER", order.getId(), order.getTotalAmount(), method, null);
            }
        }

        if (target == Order.OrderStatus.CANCELLED) {
            paymentRepository.findTopByRefTypeAndRefIdOrderByCreatedAtDesc("ORDER", order.getId())
                    .ifPresent(payment -> {
                        if ("PENDING".equals(payment.getStatus())) {
                            payment.setStatus("CANCELLED");
                            paymentRepository.save(payment);
                        }
                    });
            order.setPaymentStatus(order.getPaymentStatus() == Order.PaymentStatus.PAID
                    ? Order.PaymentStatus.REFUNDED
                    : Order.PaymentStatus.UNPAID);
            voucherService.releaseVoucher(order.getVoucherCode());
        }

        return AdminOrderResponse.fromEntity(orderRepository.save(order));
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + id));
    }
}
