package com.fitness.controller;

import com.fitness.dto.UpdateUserStatusRequest;
import com.fitness.dto.UserManagementResponse;
import com.fitness.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    // 1. GET /api/admin/users: Xem danh sách user (hỗ trợ lọc ?role=CLIENT&keyword=tuan)
    @GetMapping
    public ResponseEntity<List<UserManagementResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.getAllUsersForAdmin(role, keyword));
    }

    // 2. PUT /api/admin/users/{id}/status: Khóa hoặc Mở khóa tài khoản (ACTIVE / LOCKED)
    @PutMapping("/{id}/status")
    public ResponseEntity<UserManagementResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(id, request));
    }
}