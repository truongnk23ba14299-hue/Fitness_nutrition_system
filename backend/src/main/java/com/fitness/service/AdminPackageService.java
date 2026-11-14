package com.fitness.service;

import com.fitness.dto.CreatePackageRequest;
import com.fitness.model.WorkoutPackage;
import com.fitness.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPackageService {

    private final PackageRepository packageRepository;

    public WorkoutPackage createPackage(CreatePackageRequest request) {
        WorkoutPackage workoutPackage = new WorkoutPackage();
        workoutPackage.setTitle(request.getTitle());
        workoutPackage.setDescription(request.getDescription());
        workoutPackage.setPrice(request.getPrice());
        workoutPackage.setDurationMonths(request.getDurationMonths());
        workoutPackage.setImageUrl(request.getImageUrl());
        workoutPackage.setBenefits(request.getBenefits());
        workoutPackage.setScheduleDetails(request.getScheduleDetails());

        // Mặc định là true (đang mở) nếu Admin không truyền lên
        workoutPackage.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return packageRepository.save(workoutPackage);
    }
}