package com.fitness.controller;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.dto.PackageResponse;
import com.fitness.service.PackageDetailService;
import com.fitness.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PackageController {

    private final PackageService packageService;
    private final PackageDetailService packageDetailService;

    @GetMapping
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllActivePackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageDetailResponse> getPackageDetail(@PathVariable Long id) {
        return ResponseEntity.ok(packageDetailService.getPackageDetailById(id));
    }
}