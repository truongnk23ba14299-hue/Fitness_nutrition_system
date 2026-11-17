package com.fitness.controller;

import com.fitness.dto.BookPtRequest;
import com.fitness.dto.BookPtResponse;
import com.fitness.dto.BookingResponse;
import com.fitness.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST /api/bookings/pt
    @PostMapping("/pt")
    public ResponseEntity<BookPtResponse> bookPt(
            @Valid @RequestBody BookPtRequest request,
            Authentication authentication
    ) {
        String email = getAuthenticatedEmail(authentication);
        BookPtResponse response = bookingService.createPtBooking(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/bookings/my (Lịch hẹn PT của tôi)
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        String email = getAuthenticatedEmail(authentication);
        List<BookingResponse> bookings = bookingService.getMyBookings(email);
        return ResponseEntity.ok(bookings);
    }

    // PUT /api/bookings/{id}/cancel (Học viên hủy lịch tập)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = getAuthenticatedEmail(authentication);
        BookingResponse response = bookingService.cancelBooking(id, email);
        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedEmail(Authentication authentication) {
        return (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : null;
    }
}
