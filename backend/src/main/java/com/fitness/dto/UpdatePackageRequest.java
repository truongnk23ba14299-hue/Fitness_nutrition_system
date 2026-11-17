package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageRequest {
    private String title;
    private BigDecimal price;
    private Integer durationMonths;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private String benefits;
    private String scheduleDetails;
    private String trainerInfo;
}