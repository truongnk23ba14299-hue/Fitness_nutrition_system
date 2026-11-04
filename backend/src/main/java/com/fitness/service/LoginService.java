package com.fitness.service;

import com.fitness.config.JwtTokenProvider;
import com.fitness.dto.LoginRequest;
import com.fitness.dto.LoginResponse;
import com.fitness.dto.UserResponse;
import com.fitness.model.User;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. Xử lý Đăng Nhập
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            // Kiểm tra email và password qua Spring Security (BCrypt)
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Đăng nhập thất bại: " + e.getMessage());
        }

        // Lưu phiên đăng nhập
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Lấy thông tin user vừa đăng nhập
        User user = (User) authentication.getPrincipal();

        return new LoginResponse(jwt, "Bearer", UserResponse.fromEntity(user));
    }

    // 2. Lấy thông tin tài khoản hiện tại từ email
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng: " + email));

        return UserResponse.fromEntity(user);
    }
}
