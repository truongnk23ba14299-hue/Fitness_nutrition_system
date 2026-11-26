package com.fitness.service;

import com.fitness.dto.RegisterRequest;
import com.fitness.dto.RegisterResponse;
import com.fitness.model.User;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Kiểm tra mật khẩu xác nhận
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }

        // 2. Chuẩn hóa username và kiểm tra trùng lặp (không phân biệt hoa thường)
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Tên đăng nhập này đã được sử dụng!");
        }

        // 3. Email không bắt buộc: để trống thì tự sinh email nội bộ username@fitlife.local
        String email = (request.email() != null && !request.email().isBlank())
                ? request.email().trim().toLowerCase(Locale.ROOT)
                : username + "@fitlife.local";

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email này đã được sử dụng!");
        }

        // 4. Tạo tài khoản LOCAL mới với mật khẩu đã mã hóa BCrypt
        User user = User.builder()
                .fullName(request.fullName().trim())
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .authProvider("LOCAL")
                .role("ROLE_CLIENT")
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                "Đăng ký tài khoản thành công!",
                savedUser.getFullName(),
                savedUser.getEmail()
        );
    }
}
