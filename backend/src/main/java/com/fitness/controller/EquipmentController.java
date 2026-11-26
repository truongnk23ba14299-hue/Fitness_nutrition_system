package com.fitness.controller;

import com.fitness.dto.EquipmentRequest;
import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.dto.EquipmentResponse;
import com.fitness.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 1. GET /api/equipments: Lấy danh sách máy tập (có thể lọc: ?targetMuscle=Ngực)
    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipments(
            @RequestParam(required = false) String targetMuscle) {
        return ResponseEntity.ok(equipmentService.getAllEquipments(targetMuscle));
    }

    // 2. GET /api/equipments/{id}: Xem chi tiết 1 máy tập và cách tập
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDetailResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    // 3. POST /api/equipments: Thêm máy tập mới vào phòng gym
    @PostMapping
    public ResponseEntity<EquipmentDetailResponse> addEquipment(
            @Valid @RequestBody EquipmentRequest request) {
        EquipmentDetailResponse response = equipmentService.addEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. PUT /api/equipments/{id}: Cập nhật thông tin máy tập
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDetailResponse> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request) {
        EquipmentDetailResponse response = equipmentService.updateEquipment(id, request);
        return ResponseEntity.ok(response);
    }

    // 5. DELETE /api/equipments/{id}: Xóa máy tập khỏi phòng gym
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}