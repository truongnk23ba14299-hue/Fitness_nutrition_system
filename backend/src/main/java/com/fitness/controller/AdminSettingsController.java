package com.fitness.controller;

import com.fitness.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final AdminSettingsService settingsService;

    // 1. GET /api/admin/settings (API #52)
    @GetMapping
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    // 2. PUT /api/admin/settings (API #53)
    @PutMapping
    public ResponseEntity<Map<String, String>> updateSettings(
            Authentication authentication,
            @RequestBody Map<String, String> newSettings
    ) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(settingsService.updateSettings(adminEmail, newSettings));
    }
}