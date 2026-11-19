package com.fitness.controller;

import com.fitness.dto.CreateVoucherRequest;
import com.fitness.dto.UpdateVoucherRequest;
import com.fitness.dto.VoucherResponse;
import com.fitness.repository.VoucherRepository;
import com.fitness.service.AdminVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {
    private final VoucherRepository voucherRepository;
    private final AdminVoucherService adminVoucherService;

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAllVouchers(){
        return ResponseEntity.ok(adminVoucherService.getAllVouchers());

    }

    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(adminVoucherService.createVoucher(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> updateVoucher(@PathVariable Long id, @Valid @RequestBody UpdateVoucherRequest req){
        return ResponseEntity.ok(adminVoucherService.updateVoucher(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id){
        adminVoucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();

    }
}
