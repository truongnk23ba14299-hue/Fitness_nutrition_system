package com.fitness.controller;

import com.fitness.dto.BookingResponse;
import com.fitness.dto.UpdateBookingStatusRequest;
import com.fitness.dto.RescheduleBookingRequest;
import com.fitness.service.TrainerBookingService;
import jakarta.validation.Valid;
import com.fitness.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@CrossOrigin(origins = "*")
public class AdminBookingController {

    private final BookingService bookingService;
    private final TrainerBookingService trainerBookingService;

    public AdminBookingController(BookingService bookingService, TrainerBookingService trainerBookingService) {
        this.bookingService = bookingService;
        this.trainerBookingService = trainerBookingService;
    }

    /**
     * API Endpoint: GET /api/admin/bookings
     * Cho phép Quản trị viên (Admin) và Lễ tân xem toàn bộ danh sách các ca đặt lịch PT.
     * Hỗ trợ các bộ lọc tuỳ chọn: status, trainerId, userId, date.
     *
     * @param status Trạng thái (PENDING, CONFIRMED, CANCELLED, ...)
     * @param trainerId ID Huấn luyện viên
     * @param userId ID Học viên
     * @param date Ngày diễn ra ca tập (YYYY-MM-DD)
     * @param authentication Thông tin phiên đăng nhập
     * @return Danh sách BookingResponse
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        String email = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : null;
        List<BookingResponse> bookings = bookingService.getAdminBookings(status, trainerId, userId, date, email);
        return ResponseEntity.ok(bookings);
    }

    // PUT /api/admin/bookings/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request, Authentication authentication) {
        return ResponseEntity.ok(trainerBookingService.updateStatus(id, request, authentication.getName()));
    }

    // PUT /api/admin/bookings/{id}/reschedule
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<BookingResponse> rescheduleBooking(@PathVariable Long id,
            @Valid @RequestBody RescheduleBookingRequest request, Authentication authentication) {
        return ResponseEntity.ok(trainerBookingService.reschedule(id, request, authentication.getName()));
    }
}
