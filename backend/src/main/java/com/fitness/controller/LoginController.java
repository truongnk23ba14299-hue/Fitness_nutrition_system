package com.fitness.controller;

import com.fitness.dto.GoogleLoginRequest;
import com.fitness.dto.GoogleUserInfo;
import com.fitness.dto.LoginRequest;
import com.fitness.dto.LoginResponse;
import com.fitness.dto.UserResponse;
import com.fitness.service.GoogleAuthService;
import com.fitness.service.LoginService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoginController {
    private final LoginService loginService;
    private final GoogleAuthService googleAuthService;

    // 1. API Đăng Nhập: POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

    // 2. API Xem thông tin cá nhân: GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục");
        }
        UserResponse response = loginService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest req) {
        GoogleIdToken.Payload payload = googleAuthService.verifyGoogleToken(req.idToken());
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = getStringClaim(payload, "name");
        String picture = getStringClaim(payload, "picture");

        if (isBlank(googleId) || isBlank(email)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Google account information is invalid"
            );
        }
        // 4. Create DTO for User Service
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                googleId, email, name, picture );
        // 5. Delegate account linking / creation to User Service
        return ResponseEntity.ok(loginService.processGoogleUser(googleUserInfo));
    }
    // Safely read optional Google claims
    private String getStringClaim(GoogleIdToken.Payload payload, String claimName) {
        Object value = payload.get(claimName);
        return value instanceof String ? (String) value : null;
    }
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
