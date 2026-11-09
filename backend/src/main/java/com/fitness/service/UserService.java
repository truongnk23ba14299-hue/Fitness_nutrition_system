package com.fitness.service;

import com.fitness.dto.UpdateProfileRequest;
import com.fitness.dto.UserProfileResponse;
import com.fitness.model.User;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Xem thông tin cá nhân theo email từ Token
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

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
                user.getCreatedAt()
        );
    }

    // Cập nhật thông tin cá nhân
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setHeightCm(request.heightCm());
        user.setWeightKg(request.weightKg());
        user.setAvatarUrl(request.avatarUrl());

        User updatedUser = userRepository.save(user);

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
                updatedUser.getCreatedAt()
        );
    }
}