package com.fitness.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Ghi chú: Entity người dùng đã được hợp nhất vào com.fitness.model.User (triển khai UserDetails)
 * để tránh xung đột trùng bảng "users" trong Hibernate và đồng bộ dữ liệu giữa Login & Register.
 * Class Account này được giữ lại để tương thích cấu trúc ban đầu nếu cần tham khảo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
