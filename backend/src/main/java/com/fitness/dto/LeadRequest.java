package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String topic;

    private String message;
}