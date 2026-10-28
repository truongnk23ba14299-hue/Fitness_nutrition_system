package com.usth.fitness_project.controller;

import com.usth.fitness_project.dto.LoginRequest;
import com.usth.fitness_project.dto.LoginResponse;
import com.usth.fitness_project.dto.UserResponse;
import com.usth.fitness_project.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoginController {

    private final LoginService loginService;

    // 1. API Đăng Nhập: POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

    // 2. API Xem thông tin cá nhân: GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = loginService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
