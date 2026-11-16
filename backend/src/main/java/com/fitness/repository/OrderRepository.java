package com.fitness.repository;
<<<<<<< HEAD

import com.fitness.model.Order;
import com.fitness.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findAllByOrderByCreatedAtDesc();
}
=======
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
}


>>>>>>> 0a0714c (Update Product & Add Order API)
