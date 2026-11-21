package com.fitness.controller;

import com.fitness.dto.AdminDashboardSummaryResponse;
import com.fitness.dto.RevenueReportResponse;
import com.fitness.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminReportController {

    private final AdminReportService reportService;

    // 1. GET /api/admin/dashboard/summary (API #51)
    @GetMapping("/dashboard/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(reportService.getDashboardSummary());
    }

    // 2. GET /api/admin/reports/revenue (API #50)
    @GetMapping("/reports/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String groupBy
    ) {
        return ResponseEntity.ok(reportService.getRevenueReport(from, to, groupBy));
    }
}