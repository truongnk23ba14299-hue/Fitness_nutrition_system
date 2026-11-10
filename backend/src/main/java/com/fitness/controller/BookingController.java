package com.fitness.controller;

import com.fitness.dto.BookPtRequest;
import com.fitness.dto.BookPtResponse;
import com.fitness.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<BookPtResponse> bookPt(@Valid @RequestBody BookPtRequest request) {
        BookPtResponse response = bookingService.createPtBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
