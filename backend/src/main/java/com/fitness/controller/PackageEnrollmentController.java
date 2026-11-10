package com.fitness.controller;

import com.fitness.dto.EnrollmentRequest;
import com.fitness.dto.EnrollmentResponse;
import com.fitness.service.PackageEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/package-enrollments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PackageEnrollmentController {

    private final PackageEnrollmentService packageEnrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(
            Authentication authentication,
            @Valid @RequestBody EnrollmentRequest request
    ) {
        // authentication.getName() tự động trả về email của người dùng từ Token JWT
        String email = authentication.getName();
        EnrollmentResponse response = packageEnrollmentService.enrollPackage(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}   