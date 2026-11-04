package com.fitness.dto;

import com.fitness.model.WorkoutPackage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDetailResponse {

    private Long id;
    private String benefits;
    private String schedule;
    private String trainerInfo;

    public static PackageDetailResponse fromEntity(WorkoutPackage pkg) {
        if (pkg == null) return null;
        return new PackageDetailResponse(
                pkg.getId(),
                pkg.getBenefits(),
                pkg.getScheduleDetails(),
                pkg.getTrainerInfo()
        );
    }
}
