package com.fitness.controller;

import com.fitness.dto.TrainerClientDetailResponse;
import com.fitness.dto.TrainerClientResponse;
import com.fitness.service.TrainerClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API dành cho HLV: quản lý học viên kèm riêng.
 * Bảo mật: /api/trainer/** chỉ cho ROLE_TRAINER (và ADMIN) theo SecurityConfig.
 */
@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrainerClientController {

    private final TrainerClientService trainerClientService;

    /**
     * API Endpoint: GET /api/trainer/clients
     * HLV xem danh sách học viên đang theo học gói PT của mình
     */
    @GetMapping("/clients")
    public ResponseEntity<List<TrainerClientResponse>> getMyClients(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(trainerClientService.getMyClients(email));
    }

    /**
     * API Endpoint: GET /api/trainer/clients/{userId}
     * HLV xem chi tiết thể trạng và bệnh sử của học viên kèm riêng
     */
    @GetMapping("/clients/{userId}")
    public ResponseEntity<TrainerClientDetailResponse> getClientDetail(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(trainerClientService.getClientDetail(userId, email));
    }
}
