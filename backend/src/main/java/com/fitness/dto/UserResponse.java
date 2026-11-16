package com.fitness.dto;

import com.fitness.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private List<String> roles;

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;

        List<String> roleNames = (user.getRole() != null && !user.getRole().isBlank())
                ? List.of(user.getRole())
                : List.of("ROLE_CLIENT");

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(roleNames)
                .build();
    }
}
