package com.fitness.controller;

import com.fitness.dto.PosSaleRequest;
import com.fitness.dto.PosSaleResponse;
import com.fitness.service.StaffPosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/pos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffPosController {

    private final StaffPosService staffPosService;

    /**
     * POST /api/staff/pos/sales
     * Lễ tân bán vé ngày (Daily Pass) hoặc bán gói tập trực tiếp tại quầy POS
     */
    @PostMapping("/sales")
    public ResponseEntity<PosSaleResponse> createSale(
            Authentication authentication,
            @Valid @RequestBody PosSaleRequest request
    ) {
        String staffEmail = authentication.getName();
        PosSaleResponse response = staffPosService.processPosSale(staffEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sales/summary")
    public ResponseEntity<?> getTodayPosSummary() {
        return ResponseEntity.ok(staffPosService.getTodayPosSummary());
    }
}