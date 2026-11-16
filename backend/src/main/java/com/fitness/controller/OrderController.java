package com.fitness.controller;
import com.fitness.dto.OrderRequest;
import com.fitness.dto.OrderResponse;
import com.fitness.dto.OrderSummaryResponse;
import com.fitness.model.User;
import com.fitness.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor

public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest req, @AuthenticationPrincipal User user){
        OrderResponse response = orderService.createOrder(req, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<OrderSummaryResponse>> findAllByUser(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(orderService.getMyOrders(user));
    }

    @GetMapping("/my-orders/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderResponse> findByIdAndUserId(@PathVariable Long id, @AuthenticationPrincipal User user){
        return ResponseEntity.ok(orderService.getMyOrderById(id, user));
    }
}
