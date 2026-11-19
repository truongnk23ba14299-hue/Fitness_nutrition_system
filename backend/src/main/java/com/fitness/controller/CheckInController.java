package com.fitness.controller;

import com.fitness.dto.CheckInRequest;
import com.fitness.dto.CheckInResponse;
import com.fitness.model.User;
import com.fitness.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class CheckInController {

    private final CheckInService checkInService;

    // 1. POST /api/checkins: Quét mã QR điểm danh học viên tại quầy (chống quét < 2h trả 409)
    @PostMapping
    public ResponseEntity<CheckInResponse> recordCheckIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal User staff) {
        CheckInResponse response = checkInService.recordCheckIn(request, staff);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. GET /api/checkins: Lấy danh sách lượt check-in theo ngày hoặc N lượt gần nhất
    @GetMapping
    public ResponseEntity<List<CheckInResponse>> getCheckIns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(checkInService.getCheckIns(date, limit));
    }
}