package com.fitness.controller;

import com.fitness.dto.AdminPackageResponse;
import com.fitness.dto.CreatePackageRequest;
import com.fitness.model.WorkoutPackage;
import com.fitness.service.AdminPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/packages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminPackageController {

    private final AdminPackageService adminPackageService;

    /**
     * Admin xem tất cả gói tập (gồm cả gói ẩn isActive=false)
     * GET /api/admin/packages
     */
    @GetMapping
    public ResponseEntity<List<AdminPackageResponse>> getAllPackages() {
        return ResponseEntity.ok(adminPackageService.getAllPackages());
    }

    /**
     * Admin thêm gói tập mới
     * POST /api/admin/packages
     */
    @PostMapping
    public ResponseEntity<WorkoutPackage> createPackage(@RequestBody CreatePackageRequest request) {
        WorkoutPackage createdPackage = adminPackageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPackage);
    }
}
