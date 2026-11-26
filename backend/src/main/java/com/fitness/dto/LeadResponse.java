package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String topic;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private String responseMessage;
}