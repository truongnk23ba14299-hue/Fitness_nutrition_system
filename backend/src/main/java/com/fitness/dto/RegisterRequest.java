package com.fitness.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull(message = "Name không được để trống")
        String fullName,

        @NotNull(message = "Email không được để trống")
        @Email(message = "Sai định dạng email")
        String email,

        @NotNull(message = "Password không được để trống")
        @Size(min = 8,message = "Password tối thiểu 8 kí tự")
        String password
) {
}
