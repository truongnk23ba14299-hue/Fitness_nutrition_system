package com.fitness.controller;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.dto.PackageResponse;
import com.fitness.model.WorkoutPackage;
import com.fitness.service.PackageDetailService;
import com.fitness.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PackageController {

    private final PackageService packageService;
    private final PackageDetailService packageDetailService;

    @GetMapping("/api/packages")
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllActivePackages());
    }

    @GetMapping("/api/packages/{id}")
    public ResponseEntity<PackageDetailResponse> getPackageDetail(@PathVariable Long id) {
        return ResponseEntity.ok(packageDetailService.getPackageDetailById(id));
    }

    @GetMapping("/api/packages/{id}/statistics")
    public ResponseEntity<?> getPackageStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(packageDetailService.getPackageStatistics(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/packages/{id}")
    public ResponseEntity<WorkoutPackage> updatePackage(
            @PathVariable Long id,
            @RequestBody WorkoutPackage request) {
        return ResponseEntity.ok(packageDetailService.updatePackage(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/packages/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Long id) {
        return ResponseEntity.ok(packageDetailService.deletePackage(id));
    }
}