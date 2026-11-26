package com.fitness.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        String fullName,

        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 4, max = 30, message = "Tên đăng nhập phải từ 4 đến 30 ký tự")
        @Pattern(
                regexp = "^[a-zA-Z0-9._]+$",
                message = "Tên đăng nhập chỉ được chứa chữ cái, chữ số, dấu chấm và dấu gạch dưới"
        )
        String username,

        // Email không bắt buộc: để trống hệ thống sẽ tự sinh username@fitlife.local
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
        String password,

        @NotBlank(message = "Vui lòng xác nhận lại mật khẩu")
        String confirmPassword
) {}
