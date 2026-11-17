package com.fitness.service;

import com.fitness.dto.*;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.User;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PackageEnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Xem thông tin cá nhân kèm cờ hasActivePackage
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng!"));

        boolean hasActive = enrollmentRepository.existsByUserIdAndStatus(user.getId(), EnrollmentStatus.ACTIVE);

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAddress(),
                user.getHeightCm(),
                user.getWeightKg(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                hasActive
        );
    }

    // 2. Cập nhật thông tin cá nhân
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng!"));

        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setHeightCm(request.heightCm());
        user.setWeightKg(request.weightKg());
        user.setAvatarUrl(request.avatarUrl());

        User updatedUser = userRepository.save(user);

        boolean hasActive = enrollmentRepository.existsByUserIdAndStatus(updatedUser.getId(), EnrollmentStatus.ACTIVE);

        return new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getPhone(),
                updatedUser.getAddress(),
                updatedUser.getHeightCm(),
                updatedUser.getWeightKg(),
                updatedUser.getAvatarUrl(),
                updatedUser.getCreatedAt(),
                hasActive
        );
    }

    // 3. Đổi mật khẩu cá nhân (PUT /api/users/change-password)
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác!");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu mới không khớp!");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng với mật khẩu cũ!");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

     // 4. ADMIN: XEM DANH SÁCH USER (ĐÃ LỌC BỎ CÁC TRƯỜNG RIÊNG TƯ NHẠY CẢM)
    public List<UserManagementResponse> getAllUsersForAdmin(String role, String keyword) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(u -> {
                    if (role == null || role.isBlank()) return true;
                    String filterRole = role.trim().toUpperCase();
                    if (!filterRole.startsWith("ROLE_")) filterRole = "ROLE_" + filterRole;
                    return filterRole.equalsIgnoreCase(u.getRole());
                })
                .filter(u -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String kw = keyword.trim().toLowerCase();
                    return (u.getFullName() != null && u.getFullName().toLowerCase().contains(kw)) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw));
                })
                .map(u -> new UserManagementResponse(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole(),
                        u.getStatus() != null ? u.getStatus() : "ACTIVE",
                        u.getPhone(),
                        u.getCreatedAt()
                ))
                .toList();
    }

    // 5. ADMIN: KHÓA / MỞ KHÓA USER
    @Transactional
    public UserManagementResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id));

        if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể khóa tài khoản Quản trị viên (ADMIN)!");
        }

        user.setStatus(request.status().toUpperCase());
        User updated = userRepository.save(user);

        return new UserManagementResponse(
                updated.getId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getRole(),
                updated.getStatus(),
                updated.getPhone(),
                updated.getCreatedAt()
        );
    }
}