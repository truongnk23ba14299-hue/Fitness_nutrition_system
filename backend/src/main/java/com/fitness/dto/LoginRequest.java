package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Ô nhập liệu hợp nhất: chấp nhận cả Tên đăng nhập (username) lẫn Email.
     */
    private String identifier;

    /**
     * Giữ lại trường email cũ để tương thích 100% collection Postman / client cũ.
     */
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    /**
     * Luôn trả về một giá trị thống nhất cho tầng Service:
     * ưu tiên identifier mới, nếu client cũ chỉ gửi email thì lấy email.
     */
    public String getIdentifier() {
        if (identifier != null && !identifier.isBlank()) {
            return identifier;
        }
        return email;
    }
}
