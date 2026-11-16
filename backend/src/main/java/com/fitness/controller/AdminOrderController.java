package com.fitness.controller;

import com.fitness.dto.AdminOrderResponse;
import com.fitness.dto.UpdateOrderStatusRequest;
import com.fitness.model.Order;
import com.fitness.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    // GET /api/admin/orders            -> toàn bộ đơn hàng
    // GET /api/admin/orders?status=PENDING / ?status=COMPLETED -> lọc theo trạng thái
    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders(
            @RequestParam(required = false) Order.OrderStatus status) {
        return ResponseEntity.ok(adminOrderService.getAllOrders(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderById(id));
    }

    // PUT /api/admin/orders/{id}/status : Duyệt đơn PENDING -> CONFIRMED -> SHIPPING -> COMPLETED / CANCELLED
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminOrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest req) {
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, req.status()));
    }
}
