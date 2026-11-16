package com.fitness.service;

import com.fitness.Exception.ResourceNotFoundException;
import com.fitness.dto.AdminOrderResponse;
import com.fitness.model.Order;
import com.fitness.model.OrderStatus;
import com.fitness.model.User;
import com.fitness.repository.OrderRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // Luồng duyệt đơn chuẩn: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING, Set.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    // Xem toàn bộ đơn hàng trong phòng gym (lọc theo PENDING, COMPLETED,...)
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getAllOrders(OrderStatus status) {
        List<Order> orders = (status == null)
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);

        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Duyệt đơn hàng: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED / CANCELLED
    @Transactional
    public AdminOrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = findOrThrow(id);
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("Đơn hàng đã ở trạng thái " + newStatus);
        }

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Không thể chuyển đơn hàng từ " + currentStatus + " sang " + newStatus
                            + ". Luồng chuẩn: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + id));
    }

    private AdminOrderResponse toResponse(Order order) {
        // Lấy thông tin học viên đặt đơn (chỉ đọc, không phụ thuộc cấu trúc User của thành viên khác)
        String customerName = "Học viên #" + order.getUserId();
        String customerEmail = null;
        User customer = userRepository.findById(order.getUserId()).orElse(null);
        if (customer != null) {
            customerName = customer.getFullName();
            customerEmail = customer.getEmail();
        }
        return AdminOrderResponse.fromEntity(order, customerName, customerEmail);
    }
}
