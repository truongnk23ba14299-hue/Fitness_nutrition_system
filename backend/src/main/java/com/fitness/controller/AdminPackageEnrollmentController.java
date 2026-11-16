package com.fitness.controller;

import com.fitness.dto.AdminEnrollmentReportResponse;
import com.fitness.dto.EnrollmentResponse;
import com.fitness.dto.UpdateEnrollmentStatusRequest;
import com.fitness.service.PackageEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/package-enrollments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPackageEnrollmentController {

    private final PackageEnrollmentService packageEnrollmentService;

    // 1. GET /api/admin/package-enrollments: Báo cáo doanh thu & Tìm kiếm theo keyword
    @GetMapping
    public ResponseEntity<AdminEnrollmentReportResponse> getReport(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(packageEnrollmentService.getEnrollmentReport(keyword));
    }

    // 2. PUT /api/admin/package-enrollments/{id}/status: Đổi trạng thái gói tập
    @PutMapping("/{id}/status")
    public ResponseEntity<EnrollmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request) {
        return ResponseEntity.ok(packageEnrollmentService.updateStatus(id, request.status()));
    }
}