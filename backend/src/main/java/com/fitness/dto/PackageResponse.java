package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer durationMonths;
    private String description;
    private String imageUrl;
}
