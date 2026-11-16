package com.fitness.repository;

import com.fitness.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "user"})
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    // ==== Minh Quang: phục vụ GET /api/admin/orders + lọc theo trạng thái ====
    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findByOrderStatusOrderByCreatedAtDesc(Order.OrderStatus status);
}
