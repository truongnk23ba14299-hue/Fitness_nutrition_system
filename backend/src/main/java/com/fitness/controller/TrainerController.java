package com.fitness.controller;

import com.fitness.dto.TrainerResponse;
import com.fitness.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@CrossOrigin(origins = "*")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    /**
     * API Endpoint: GET /api/trainers
     * Lấy danh sách các huấn luyện viên (PT), hỗ trợ query param lọc theo chuyên môn: ?specialty=...
     *
     * @param specialty (Tuỳ chọn) Tên chuyên môn thể hình / gym cần lọc
     * @return Danh sách PT dưới dạng TrainerResponse
     */
    @GetMapping
    public ResponseEntity<List<TrainerResponse>> getAllTrainers(
            @RequestParam(required = false) String specialty) {
        List<TrainerResponse> trainers = trainerService.getAllTrainers(specialty);
        return ResponseEntity.ok(trainers);
    }

    /**
     * API Endpoint: GET /api/trainers/{id}
     * Lấy chi tiết thông tin của 1 PT theo ID
     *
     * @param id ID của PT
     * @return Chi tiết PT hoặc 404 NOT FOUND nếu không tồn tại
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrainerResponse> getTrainerById(@PathVariable Long id) {
        TrainerResponse trainer = trainerService.getTrainerById(id);
        if (trainer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trainer);
    }
}
