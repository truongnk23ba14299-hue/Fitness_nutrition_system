package com.fitness.controller;

import com.fitness.dto.CreatePackageRequest;
import com.fitness.model.WorkoutPackage;
import com.fitness.service.AdminPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/packages")
@RequiredArgsConstructor
public class AdminPackageController {

    private final AdminPackageService adminPackageService;

    // API nhận request POST từ Admin để thêm gói tập mới
    @PostMapping
    public ResponseEntity<WorkoutPackage> createPackage(@RequestBody CreatePackageRequest request) {
        WorkoutPackage createdPackage = adminPackageService.createPackage(request);
        // Trả về HTTP Status 201 (Created) kèm theo đối tượng vừa tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPackage);
    }
}
