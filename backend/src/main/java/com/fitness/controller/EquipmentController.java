package com.fitness.controller;

import com.fitness.dto.EquipmentDeleteResponse;
import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.dto.EquipmentRequest;
import com.fitness.dto.EquipmentResponse;
import com.fitness.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 1. GET /api/equipments: Lấy danh sách tóm tắt máy tập (Public - ẩn videoUrl & workoutGuide)
    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipments(
            @RequestParam(required = false) String targetMuscle) {
        return ResponseEntity.ok(equipmentService.getAllEquipments(targetMuscle));
    }

    // 2. GET /api/equipments/{id}: Xem chi tiết máy tập + video hướng dẫn (Phải đăng nhập)
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDetailResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    // 3. POST /api/equipments: Admin thêm máy tập mới
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EquipmentDetailResponse> addEquipment(
            @Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.addEquipment(request));
    }

    // 4. PUT /api/equipments/{id}: Admin cập nhật thông tin & video hướng dẫn máy tập
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDetailResponse> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, request));
    }

    // 5. DELETE /api/equipments/{id}: Admin xóa máy tập
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<EquipmentDeleteResponse> deleteEquipment(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.deleteEquipment(id));
    }
}