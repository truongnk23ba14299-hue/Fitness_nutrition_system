package com.fitness.service;

import com.fitness.config.JwtTokenProvider;
import com.fitness.dto.GoogleUserInfo;
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

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Đăng nhập LOCAL: chấp nhận cả Tên đăng nhập (username) lẫn Email trên cùng một ô nhập liệu.
    public LoginResponse login(LoginRequest request) {
        String identifier = request.getIdentifier() == null ? "" : request.getIdentifier().trim();
        if (identifier.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng nhập tên đăng nhập hoặc email"
            );
        }

        // Chặn tài khoản Google trước khi Spring Security/BCrypt xử lý password null.
        User found = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(identifier, identifier)
                .orElse(null);
        if (found != null && found.getPassword() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tài khoản này đăng ký bằng Google, vui lòng chọn Tiếp tục với Google"
            );
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!");
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập/Email hoặc mật khẩu không chính xác");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Đăng nhập thất bại");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        return new LoginResponse(jwt, "Bearer", UserResponse.fromEntity(user));
    }

    /**
     * Xử lý tài khoản sau khi GoogleAuthService đã xác minh ID token.
     * Không tạo user trùng email; giữ nguyên tài khoản LOCAL và dữ liệu nghiệp vụ.
     */
    @Transactional
    public LoginResponse processGoogleUser(GoogleUserInfo googleInfo) {
        if (googleInfo == null || isBlank(googleInfo.googleId()) || isBlank(googleInfo.email())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thông tin tài khoản Google không hợp lệ");
        }

        String googleId = googleInfo.googleId().trim();
        String email = googleInfo.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            // Email đã tồn tại: liên kết Google vào chính tài khoản cũ.
            user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user != null) {
                if (!isBlank(user.getGoogleId()) && !googleId.equals(user.getGoogleId())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email này đã được liên kết với một tài khoản Google khác"
                    );
                }
                user.setGoogleId(googleId);
                if (isBlank(user.getAvatarUrl()) && !isBlank(googleInfo.picture())) {
                    user.setAvatarUrl(googleInfo.picture());
                }
                // Giữ authProvider=LOCAL nếu tài khoản vốn đăng ký bằng email/mật khẩu.
                user = userRepository.save(user);
            } else {
                // Email chưa tồn tại: tạo tài khoản Google mới.
                String username = generateUniqueUsername(email);
                String fullName = !isBlank(googleInfo.name())
                        ? googleInfo.name().trim()
                        : email.substring(0, email.indexOf('@'));

                user = User.builder()
                        .fullName(fullName)
                        .email(email)
                        .username(username)
                        .googleId(googleId)
                        .authProvider("GOOGLE")
                        .password(null)
                        .role("ROLE_CLIENT")
                        .status("ACTIVE")
                        .avatarUrl(googleInfo.picture())
                        .build();
                user = userRepository.save(user);
            }
        } else {
            // Google ID đã tồn tại: đồng bộ các thông tin còn thiếu, không tạo user mới.
            if (!email.equalsIgnoreCase(user.getEmail())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Google ID đã được liên kết với email khác"
                );
            }
            if (isBlank(user.getAvatarUrl()) && !isBlank(googleInfo.picture())) {
                user.setAvatarUrl(googleInfo.picture());
            }
            user = userRepository.save(user);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        return new LoginResponse(jwt, "Bearer", UserResponse.fromEntity(user));
    }

    private String generateUniqueUsername(String email) {
        String localPart = email.substring(0, email.indexOf('@'));
        String base = localPart.replaceAll("[^a-zA-Z0-9._]", "");
        if (base.length() < 4) {
            base = ("user" + base).substring(0, Math.min(30, ("user" + base).length()));
        }
        if (base.length() > 30) base = base.substring(0, 30);

        String candidate = base;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            String suffix = "_" + ThreadLocalRandom.current().nextInt(100, 1000);
            int allowedBaseLength = 30 - suffix.length();
            candidate = base.substring(0, Math.min(base.length(), allowedBaseLength)) + suffix;
        }
        return candidate;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng: " + email));
        return UserResponse.fromEntity(user);
    }
}
