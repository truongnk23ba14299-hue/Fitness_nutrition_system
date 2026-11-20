package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer durationMonths;
    private String description;
    private String imageUrl;
    private String benefits;
    private String scheduleDetails;
    private String trainerInfo;

    public PackageResponse(Long id, String title, BigDecimal price, Integer durationMonths, String description, String imageUrl, String benefits, String scheduleDetails) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.durationMonths = durationMonths;
        this.description = description;
        this.imageUrl = imageUrl;
        this.benefits = benefits;
        this.scheduleDetails = scheduleDetails;
    }
}
