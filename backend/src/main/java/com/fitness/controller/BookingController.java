package com.fitness.controller;

import com.fitness.dto.BookPtRequest;
import com.fitness.dto.BookPtResponse;
import com.fitness.dto.BookingResponse;
import com.fitness.dto.ReviewRequest;
import com.fitness.dto.ReviewResponse;
import com.fitness.service.TrainerBookingService;
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
    private final TrainerBookingService trainerBookingService;

    public BookingController(BookingService bookingService, TrainerBookingService trainerBookingService) {
        this.bookingService = bookingService;
        this.trainerBookingService = trainerBookingService;
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


    // GET /api/bookings/{id}/review - học viên xem đánh giá buổi tập của mình
    @GetMapping("/{id}/review")
    public ResponseEntity<ReviewResponse> getBookingReview(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(trainerBookingService.getBookingReview(id, getAuthenticatedEmail(authentication)));
    }

    // POST /api/bookings/{id}/review - học viên đánh giá buổi tập đã hoàn thành
    @PostMapping("/{id}/review")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerBookingService.createReview(id, request, getAuthenticatedEmail(authentication)));
    }

    private String getAuthenticatedEmail(Authentication authentication) {
        return (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : null;
    }
}
