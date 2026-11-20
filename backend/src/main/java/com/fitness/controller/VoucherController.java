package com.fitness.controller;

import com.fitness.dto.ValidateVoucherRequest;
import com.fitness.dto.ValidateVoucherResponse;
import com.fitness.dto.VoucherResponse;
import com.fitness.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoucherController {

    private final VoucherService voucherService;

    /**
     * 1. Lấy danh sách voucher hợp lệ theo scope (PACKAGE, PRODUCT, ALL)
     * GET /api/vouchers?scope=PACKAGE
     */
    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAvailableVouchers(
            @RequestParam(name = "scope", required = false, defaultValue = "ALL") String scope) {
        return ResponseEntity.ok(voucherService.getAvailableVouchers(scope));
    }

    /**
     * 2. Kiểm tra mã giảm giá & tính toán số tiền giảm khi thanh toán
     * POST /api/vouchers/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateVoucherResponse> validateVoucher(
            @Valid @RequestBody ValidateVoucherRequest req) {
        return ResponseEntity.ok(voucherService.validateVoucher(req));
    }
}
