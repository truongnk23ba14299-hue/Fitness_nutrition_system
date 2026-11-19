package com.fitness.controller;

import com.fitness.dto.*;
import com.fitness.service.TrainerBookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin(origins = "*")
public class TrainerBookingController {
    private final TrainerBookingService service;
    public TrainerBookingController(TrainerBookingService service) { this.service = service; }

    // GET /api/trainer/bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyTrainerBookings(Authentication auth) {
        return ResponseEntity.ok(service.getTrainerBookings(auth.getName()));
    }

    // PUT /api/trainer/bookings/{id}/complete
    @PutMapping("/bookings/{id}/complete")
    public ResponseEntity<BookingResponse> complete(@PathVariable Long id,
            @Valid @RequestBody(required = false) CompleteBookingRequest request, Authentication auth) {
        return ResponseEntity.ok(service.completeBooking(id, request, auth.getName()));
    }

    // PUT /api/trainer/schedule/blocked-slots
    @PutMapping("/schedule/blocked-slots")
    public ResponseEntity<String> updateBlockedSlot(@Valid @RequestBody BlockedSlotRequest request, Authentication auth) {
        return ResponseEntity.ok(service.updateBlockedSlot(request, auth.getName()));
    }
}
