package com.fitness.dto;

public record PackageStatisticsResponse(
        Long packageId,
        String packageTitle,
        Boolean isActive,
        long activeStudents,
        long totalEnrollments
) {}