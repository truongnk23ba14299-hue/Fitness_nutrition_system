package com.fitness.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DiaryEntryResponse {
    private Long id;
    private LocalDate entryDate;
    private String entryType;
    private String title;
    private Integer calories;
    private String loggedTime;
    private LocalDateTime createdAt;
}