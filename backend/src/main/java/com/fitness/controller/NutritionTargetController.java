package com.fitness.controller;

import com.fitness.dto.NutritionTargetResponse;
import com.fitness.model.User;
import com.fitness.repository.UserRepository;
import com.fitness.service.NutritionTargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NutritionTargetController {

    private final NutritionTargetService nutritionTargetService;
    private final UserRepository userRepository;

    @GetMapping("/{id}/nutrition-target")
    @PreAuthorize("hasAnyRole('CLIENT', 'TRAINER', 'ADMIN')")
    public ResponseEntity<NutritionTargetResponse> getNutritionTarget(
            @PathVariable Long id,
            Authentication authentication) {

        Long currentUserId = resolveUserId(authentication);
        String currentRole = resolveRole(authentication);

        return ResponseEntity.ok(
                nutritionTargetService.getNutritionTarget(id, currentUserId, currentRole)
        );
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            return userRepository.findByEmail(name)
                    .map(User::getId)
                    .orElse(null);
        }
    }

    private String resolveRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
            return "";
        }
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}