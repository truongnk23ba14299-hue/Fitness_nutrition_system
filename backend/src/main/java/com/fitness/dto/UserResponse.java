package com.usth.fitness_project.dto;

import com.usth.fitness_project.model.Account;
import com.usth.fitness_project.model.Role;
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
    private String phone;
    private List<String> roles;

    public static UserResponse fromEntity(Account account) {
        if (account == null) return null;

        List<String> roleNames = (account.getRoles() != null)
                ? account.getRoles().stream().map(Role::getName).toList()
                : List.of();

        return UserResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .roles(roleNames)
                .build();
    }
}
