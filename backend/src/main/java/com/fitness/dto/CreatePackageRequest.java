package com.fitness.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePackageRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer durationMonths;
    private String imageUrl;
    private String benefits;
    private String scheduleDetails;
    private Boolean isActive;
}
