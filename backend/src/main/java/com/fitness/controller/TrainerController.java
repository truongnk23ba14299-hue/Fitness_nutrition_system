package com.fitness.controller;

import com.fitness.dto.TrainerResponse;
import com.fitness.dto.TrainerScheduleResponse;
import com.fitness.service.TrainerService;
import com.fitness.service.TrainerBookingService;
import com.fitness.dto.ReviewResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@CrossOrigin(origins = "*")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainerBookingService trainerBookingService;

    public TrainerController(TrainerService trainerService, TrainerBookingService trainerBookingService) {
        this.trainerService = trainerService;
        this.trainerBookingService = trainerBookingService;
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

    /**
     * API Endpoint: GET /api/trainers/{id}/reviews
     * Lấy danh sách đánh giá của một huấn luyện viên.
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getTrainerReviews(@PathVariable Long id) {
        return ResponseEntity.ok(trainerBookingService.getTrainerReviews(id));
    }

    /**
     * API Endpoint: GET /api/trainers/{id}/schedule
     * Lấy lịch bận / trống của PT theo ngày (mặc định hôm nay nếu không truyền ?date=YYYY-MM-DD)
     *
     * @param id ID của PT
     * @param date (Tuỳ chọn) Ngày cần tra cứu lịch
     * @return TrainerScheduleResponse chứa danh sách khung giờ làm việc và trạng thái bận/trống
     */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<TrainerScheduleResponse> getTrainerSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        TrainerScheduleResponse schedule = trainerService.getTrainerSchedule(id, date);
        return ResponseEntity.ok(schedule);
    }
}
