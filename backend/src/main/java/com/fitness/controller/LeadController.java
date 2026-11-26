package com.fitness.controller;

import com.fitness.dto.LeadRequest;
import com.fitness.dto.LeadResponse;
import com.fitness.model.ContactLead;
import com.fitness.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadService leadService;

    //Khách hàng gửi form tư vấn từ Landing Page / Contact
    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody LeadRequest request) {
        LeadResponse response = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Xem danh sách khách đăng ký (Dành cho Lễ tân / Admin)
    @GetMapping
    public ResponseEntity<List<ContactLead>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }
}