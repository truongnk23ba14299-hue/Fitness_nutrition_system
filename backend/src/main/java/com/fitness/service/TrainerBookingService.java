package com.fitness.service;

import com.fitness.dto.*;
import com.fitness.model.*;
import com.fitness.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class TrainerBookingService {
    private static final List<String> ACTIVE = List.of("PENDING", "CONFIRMED");
    private final BookingRepository bookings;
    private final TrainerRepository trainers;
    private final UserRepository users;
    private final TrainerReviewRepository reviews;
    private final TrainerBlockedSlotRepository blockedSlots;

    public TrainerBookingService(BookingRepository bookings, TrainerRepository trainers,
            UserRepository users, TrainerReviewRepository reviews,
            TrainerBlockedSlotRepository blockedSlots) {
        this.bookings = bookings; this.trainers = trainers; this.users = users;
        this.reviews = reviews; this.blockedSlots = blockedSlots;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getTrainerReviews(Long trainerId) {
        if (!trainers.existsById(trainerId)) throw notFound("Không tìm thấy HLV");
        return reviews.findByTrainer_IdOrderByCreatedAtDesc(trainerId).stream()
                .map(ReviewResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse getBookingReview(Long bookingId, String email) {
        User user = currentUser(email);
        Booking booking = booking(bookingId);
        if (!booking.getUser().getId().equals(user.getId())) throw forbidden("Bạn không có quyền xem đánh giá của buổi tập này");
        return reviews.findByBooking_Id(bookingId).map(ReviewResponse::fromEntity)
                .orElseThrow(() -> notFound("Buổi tập này chưa có đánh giá"));
    }

    @Transactional
    public ReviewResponse createReview(Long bookingId, ReviewRequest request, String email) {
        User user = currentUser(email);
        Booking booking = booking(bookingId);
        if (!booking.getUser().getId().equals(user.getId())) throw forbidden("Bạn chỉ có thể đánh giá buổi tập của mình");
        if (!"COMPLETED".equalsIgnoreCase(booking.getStatus())) throw badRequest("Chỉ có thể đánh giá buổi tập đã hoàn thành");
        if (reviews.existsByBooking_Id(bookingId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Buổi tập này đã được đánh giá");
        TrainerReview review = TrainerReview.builder().booking(booking).trainer(booking.getTrainer())
                .user(user).rating(request.rating()).comment(request.comment()).build();
        TrainerReview saved = reviews.save(review);
        List<TrainerReview> all = reviews.findByTrainer_Id(booking.getTrainer().getId());
        double average = all.stream().mapToInt(TrainerReview::getRating).average().orElse(0.0);
        booking.getTrainer().setRating(Math.round(average * 100.0) / 100.0);
        trainers.save(booking.getTrainer());
        return ReviewResponse.fromEntity(saved);
    }

    @Transactional
    public BookingResponse updateStatus(Long id, UpdateBookingStatusRequest request, String email) {
        User actor = currentUser(email); requireStaff(actor);
        Booking b = booking(id); String status = request.status().toUpperCase(Locale.ROOT);
        if ("COMPLETED".equalsIgnoreCase(b.getStatus())) throw badRequest("Không thể đổi trạng thái buổi tập đã hoàn thành");
        b.setStatus(status);
        return BookingResponse.fromEntity(bookings.save(b));
    }

    @Transactional
    public BookingResponse reschedule(Long id, RescheduleBookingRequest request, String email) {
        User actor = currentUser(email); requireStaff(actor);
        Booking b = booking(id);
        if (!ACTIVE.contains(b.getStatus().toUpperCase(Locale.ROOT))) throw badRequest("Chỉ đổi giờ lịch đang chờ hoặc đã xác nhận");
        LocalDateTime newTime = request.bookingDateTime();
        if (!newTime.isAfter(LocalDateTime.now())) throw badRequest("Thời gian mới phải ở tương lai");
        if (bookings.existsByTrainer_IdAndBookingDateTimeAndStatusInAndIdNot(
                b.getTrainer().getId(), newTime, ACTIVE, id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "HLV đã có lịch vào khung giờ này");
        if (blockedSlots.findByTrainer_IdAndSlotStart(b.getTrainer().getId(), newTime).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "HLV đã khóa khung giờ này");
        b.setBookingDateTime(newTime);
        return BookingResponse.fromEntity(bookings.save(b));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getTrainerBookings(String email) {
        Trainer trainer = currentTrainer(email);
        return bookings.findByTrainer_IdOrderByBookingDateTimeAsc(trainer.getId()).stream()
                .map(BookingResponse::fromEntity).toList();
    }

    @Transactional
    public BookingResponse completeBooking(Long id, CompleteBookingRequest request, String email) {
        Trainer trainer = currentTrainer(email); Booking b = booking(id);
        if (!b.getTrainer().getId().equals(trainer.getId())) throw forbidden("Bạn không có quyền cập nhật lịch của HLV khác");
        if (!"CONFIRMED".equalsIgnoreCase(b.getStatus())) throw badRequest("Chỉ hoàn thành được lịch đã xác nhận");
        b.setStatus("COMPLETED"); b.setCompletedAt(LocalDateTime.now());
        b.setTrainerNote(request == null ? null : request.trainerNote());
        return BookingResponse.fromEntity(bookings.save(b));
    }

    @Transactional
    public String updateBlockedSlot(BlockedSlotRequest request, String email) {
        Trainer trainer = currentTrainer(email);
        LocalDateTime start = request.slotStart();
        if (!start.isAfter(LocalDateTime.now())) throw badRequest("Khung giờ phải ở tương lai");
        if (Boolean.TRUE.equals(request.blocked())) {
            if (bookings.existsByTrainer_IdAndBookingDateTimeAndStatusIn(trainer.getId(), start, ACTIVE))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể khóa giờ đã có lịch đặt");
            TrainerBlockedSlot slot = blockedSlots.findByTrainer_IdAndSlotStart(trainer.getId(), start)
                    .orElseGet(() -> TrainerBlockedSlot.builder().trainer(trainer).slotStart(start).build());
            slot.setReason(request.reason()); blockedSlots.save(slot);
            return "Đã khóa khung giờ " + start;
        }
        blockedSlots.findByTrainer_IdAndSlotStart(trainer.getId(), start).ifPresent(blockedSlots::delete);
        return "Đã mở khung giờ " + start;
    }

    private User currentUser(String email) {
        if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        return users.findByEmail(email).orElseThrow(() -> notFound("Không tìm thấy tài khoản"));
    }
    private Trainer currentTrainer(String email) {
        User u = currentUser(email);
        return trainers.findByUser_Id(u.getId())
                .orElseThrow(() -> forbidden("Tài khoản chưa được liên kết với hồ sơ HLV"));
    }
    private Booking booking(Long id) { return bookings.findByIdWithDetails(id).orElseThrow(() -> notFound("Không tìm thấy lịch đặt ID " + id)); }
    private void requireStaff(User u) {
        String role = u.getRole() == null ? "" : u.getRole().toUpperCase(Locale.ROOT);
        if (!(role.contains("ADMIN") || role.contains("STAFF") || role.contains("RECEPTIONIST"))) throw forbidden("Chỉ STAFF hoặc ADMIN mới có quyền thực hiện");
    }
    private ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
    private ResponseStatusException forbidden(String m) { return new ResponseStatusException(HttpStatus.FORBIDDEN, m); }
    private ResponseStatusException badRequest(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
