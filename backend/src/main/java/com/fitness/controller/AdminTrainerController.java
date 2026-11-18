package com.fitness.controller;

import com.fitness.dto.TrainerRequest;
import com.fitness.dto.TrainerResponse;
import com.fitness.dto.UpdateTrainerStatusRequest;
import com.fitness.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trainers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrainerController {

    private final TrainerService trainerService;

    // 1. GET /api/admin/trainers: Danh sách HLV quản trị (hỗ trợ ?keyword=gym&isActive=true)
    @GetMapping
    public ResponseEntity<List<TrainerResponse>> getAllTrainers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(trainerService.getAllTrainersForAdmin(keyword, isActive));
    }

    // 2. GET /api/admin/trainers/{id}: Xem chi tiết 1 HLV
    @GetMapping("/{id}")
    public ResponseEntity<TrainerResponse> getTrainerById(@PathVariable Long id) {
        TrainerResponse response = trainerService.getTrainerById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 3. POST /api/admin/trainers: Thêm mới HLV
    @PostMapping
    public ResponseEntity<TrainerResponse> createTrainer(@Valid @RequestBody TrainerRequest request) {
        TrainerResponse created = trainerService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. PUT /api/admin/trainers/{id}: Sửa thông tin chuyên môn, năm kinh nghiệm, bio
    @PutMapping("/{id}")
    public ResponseEntity<TrainerResponse> updateTrainer(
            @PathVariable Long id,
            @Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(trainerService.updateTrainer(id, request));
    }

    // 5. PUT /api/admin/trainers/{id}/status: Kích hoạt hoặc Vô hiệu hóa HLV (isActive: true/false)
    @PutMapping("/{id}/status")
    public ResponseEntity<TrainerResponse> updateTrainerStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrainerStatusRequest request) {
        return ResponseEntity.ok(trainerService.updateTrainerStatus(id, request.isActive()));
    }
}