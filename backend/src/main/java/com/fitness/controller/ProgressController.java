package com.fitness.controller;

import com.fitness.dto.BodyRecordResponse;
import com.fitness.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressService progressService;

    /**
     * API Endpoint: GET /api/progress/my-records
     * Học viên xem lịch sử chỉ số InBody của mình (dùng vẽ biểu đồ tiến độ)
     */
    @GetMapping("/my-records")
    public ResponseEntity<List<BodyRecordResponse>> getMyRecords(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(progressService.getMyRecords(email));
    }

    /**
     * API Endpoint: DELETE /api/progress/my-records/{id}
     * Học viên xóa bản ghi chỉ số đo InBody bị nhập nhầm
     */
    @DeleteMapping("/my-records/{id}")
    public ResponseEntity<Map<String, String>> deleteMyRecord(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        progressService.deleteMyRecord(id, email);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bản ghi chỉ số InBody thành công"));
    }
}
