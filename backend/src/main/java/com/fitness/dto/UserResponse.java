package com.fitness.dto;

import com.fitness.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Double heightCm;
    private Double weightKg;
    private String avatarUrl;
    private List<String> roles;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;

        List<String> roleNames = (user.getRole() != null && !user.getRole().isBlank())
                ? List.of(user.getRole())
                : List.of("ROLE_CLIENT");

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .heightCm(user.getHeightCm())
                .weightKg(user.getWeightKg())
                .avatarUrl(user.getAvatarUrl())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
