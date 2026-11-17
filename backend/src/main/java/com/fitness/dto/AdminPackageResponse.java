package com.fitness.dto;

import com.fitness.model.WorkoutPackage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPackageResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer durationMonths;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private String benefits;
    private String scheduleDetails;
    private String trainerInfo;

    public static AdminPackageResponse fromEntity(WorkoutPackage pkg) {
        if (pkg == null) return null;
        return new AdminPackageResponse(
                pkg.getId(),
                pkg.getTitle(),
                pkg.getPrice(),
                pkg.getDurationMonths(),
                pkg.getDescription(),
                pkg.getImageUrl(),
                pkg.getIsActive(),
                pkg.getBenefits(),
                pkg.getScheduleDetails(),
                pkg.getTrainerInfo()
        );
    }
}