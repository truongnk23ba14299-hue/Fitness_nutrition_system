package com.example.demo.service;

import com.example.demo.dto.PackageResponse;
import com.example.demo.model.WorkoutPackage;
import com.example.demo.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    public List<PackageResponse> getAllActivePackages() {
        List<WorkoutPackage> packages = packageRepository.findByIsActiveTrue();

        // turn to DTO
        return packages.stream().map(pkg -> new PackageResponse(
                pkg.getId(),
                pkg.getTitle(),
                pkg.getPrice(),
                pkg.getDurationMonths(),
                pkg.getDescription(),
                pkg.getImageUrl()
        )).collect(Collectors.toList());
    }
}