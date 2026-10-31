package com.fitness.controller;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.service.PackageDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*")
public class PackageDetailController {

    private final PackageDetailService service;

    public PackageDetailController(PackageDetailService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageDetailResponse> getPackageDetail(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getPackageDetailById(id));
    }
}
