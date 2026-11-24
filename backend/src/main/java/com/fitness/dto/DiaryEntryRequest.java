package com.fitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiaryEntryRequest {
    private LocalDate entryDate;

    @NotBlank(message = "Loại nhật ký không được để trống (MEAL hoặc WORKOUT)")
    private String entryType;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotNull(message = "Lượng calo không được để trống")
    @Min(value = 0, message = "Calo phải lớn hơn hoặc bằng 0")
    private Integer calories;

    private String loggedTime;
}