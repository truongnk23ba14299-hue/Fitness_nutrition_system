package com.fitness.controller;

import com.fitness.dto.TrainerProfileResponse;
import com.fitness.dto.TrainerProfileUpdateRequest;
import com.fitness.service.TrainerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API dành cho HLV: hồ sơ cá nhân (Bio) của chính mình.
 * Bảo mật: /api/trainer/** chỉ cho ROLE_TRAINER (và ADMIN) theo SecurityConfig.
 */
@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrainerProfileController {

    private final TrainerProfileService trainerProfileService;

    /**
     * API Endpoint: GET /api/trainer/profile
     * HLV lấy thông tin Bio cá nhân khi đăng nhập
     */
    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(trainerProfileService.getMyProfile(email));
    }

    /**
     * API Endpoint: PUT /api/trainer/profile
     * HLV tự cập nhật Bio và kinh nghiệm cá nhân
     */
    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody TrainerProfileUpdateRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(trainerProfileService.updateMyProfile(email, request));
    }
}
