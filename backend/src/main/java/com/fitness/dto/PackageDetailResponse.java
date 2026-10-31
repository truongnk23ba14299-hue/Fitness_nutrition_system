package com.fitness.dto;

import com.fitness.model.PackageDetail;

public class PackageDetailResponse {

    private Long id;
    private String benefits;
    private String schedule;
    private String trainerInfo;

    public PackageDetailResponse(Long id, String benefits,
                                  String schedule, String trainerInfo) {
        this.id = id;
        this.benefits = benefits;
        this.schedule = schedule;
        this.trainerInfo = trainerInfo;
    }

    public static PackageDetailResponse fromEntity(PackageDetail detail) {
        return new PackageDetailResponse(
                detail.getId(),
                detail.getBenefits(),
                detail.getSchedule(),
                detail.getTrainerInfo()
        );
    }

    public Long getId() {
        return id;
    }

    public String getBenefits() {
        return benefits;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getTrainerInfo() {
        return trainerInfo;
    }
}
