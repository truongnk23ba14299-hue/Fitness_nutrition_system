package com.usth.fitness_project.service;

import com.usth.fitness_project.config.JwtTokenProvider;
import com.usth.fitness_project.dto.LoginRequest;
import com.usth.fitness_project.dto.LoginResponse;
import com.usth.fitness_project.dto.UserResponse;
import com.usth.fitness_project.model.Account;
import com.usth.fitness_project.repository.LoginAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final LoginAccountRepository loginAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. Xử lý Đăng Nhập
    public LoginResponse login(LoginRequest request) {
        // Kiểm tra email và password qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Lưu phiên đăng nhập
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Lấy thông tin account vừa đăng nhập
        Account account = (Account) authentication.getPrincipal();

        return new LoginResponse(jwt, "Bearer", UserResponse.fromEntity(account));
    }

    // 2. Lấy thông tin tài khoản hiện tại từ email
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        Account account = loginAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + email));

        return UserResponse.fromEntity(account);
    }
}
