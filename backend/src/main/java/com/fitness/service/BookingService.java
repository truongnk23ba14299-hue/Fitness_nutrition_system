
package com.fitness.service;

import com.fitness.dto.BookPtRequest;
import com.fitness.dto.BookPtResponse;
import com.fitness.dto.BookingResponse;
import com.fitness.model.Booking;
import com.fitness.model.Trainer;
import com.fitness.model.User;
import com.fitness.repository.BookingRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class BookingService {

    private static final List<String> ACTIVE_STATUSES =
            List.of("PENDING", "CONFIRMED");

    private final BookingRepository bookingRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            TrainerRepository trainerRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
    }

    // Đặt lịch PT
    @Transactional
    public BookPtResponse createPtBooking(BookPtRequest request) {
        return createPtBooking(request, null);
    }

    @Transactional
    public BookPtResponse createPtBooking(
            BookPtRequest request,
            String currentUserEmail) {

        User user = resolveUser(request, currentUserEmail);

        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy huấn luyện viên"));

        if (!Boolean.TRUE.equals(trainer.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Huấn luyện viên hiện không hoạt động");
        }

        boolean slotTaken =
                bookingRepository.existsByTrainer_IdAndBookingDateTimeAndStatusIn(
                        trainer.getId(),
                        request.bookingDateTime(),
                        ACTIVE_STATUSES);

        if (slotTaken) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Khung giờ này đã được đặt. Vui lòng chọn giờ khác");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTrainer(trainer);
        booking.setBookingDateTime(request.bookingDateTime());
        booking.setNote(request.note());
        booking.setStatus("PENDING");

        return BookPtResponse.fromEntity(
                bookingRepository.save(booking));
    }

    // Học viên xem lịch của mình
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(
            String currentUserEmail) {

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Người dùng chưa đăng nhập");
        }

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông tin người dùng"));

        List<Booking> bookings =
                bookingRepository.findByUserIdWithDetails(user.getId());

        return bookings.stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    // Hủy lịch hẹn
    @Transactional
    public BookingResponse cancelBooking(
            Long bookingId,
            String currentUserEmail) {

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Người dùng chưa đăng nhập");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lịch đặt với ID: " + bookingId));

        User authUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông tin người dùng"));

        boolean isOwner = booking.getUser() != null
                && booking.getUser().getId().equals(authUser.getId());

        boolean isStaff = authUser.getRole() != null &&
                (authUser.getRole().toUpperCase(Locale.ROOT).contains("ADMIN")
                || authUser.getRole().toUpperCase(Locale.ROOT)
                        .contains("RECEPTIONIST"));

        if (!isOwner && !isStaff) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền hủy lịch hẹn này");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lịch hẹn này đã được hủy trước đó");
        }

        if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không thể hủy lịch hẹn đã hoàn thành");
        }

        booking.setStatus("CANCELLED");

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    // Admin xem và lọc toàn bộ lịch đặt PT
    @Transactional(readOnly = true)
    public List<BookingResponse> getAdminBookings(
            String status,
            Long trainerId,
            Long userId,
            LocalDate date,
            String currentUserEmail) {

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Người dùng chưa đăng nhập");
        }

        User authUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông tin người dùng"));

        boolean isStaff = authUser.getRole() != null &&
                (authUser.getRole().toUpperCase(Locale.ROOT).contains("ADMIN")
                || authUser.getRole().toUpperCase(Locale.ROOT)
                        .contains("RECEPTIONIST"));

        if (!isStaff) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ Admin hoặc Receptionist mới có quyền truy cập");
        }

        String filterStatus = (status != null && !status.isBlank())
                ? status.trim()
                : null;

        List<Booking> bookings =
                bookingRepository.findAllByOrderByBookingDateTimeDesc();

        return bookings.stream()

                .filter(b -> filterStatus == null ||
                        (b.getStatus() != null &&
                         b.getStatus().equalsIgnoreCase(filterStatus)))

                .filter(b -> trainerId == null ||
                        (b.getTrainer() != null &&
                         trainerId.equals(b.getTrainer().getId())))

                .filter(b -> userId == null ||
                        (b.getUser() != null &&
                         userId.equals(b.getUser().getId())))

                .filter(b -> date == null ||
                        (b.getBookingDateTime() != null &&
                         b.getBookingDateTime().toLocalDate().equals(date)))

                .map(BookingResponse::fromEntity)
                .toList();
    }

    // Xác định người đặt lịch
    private User resolveUser(
            BookPtRequest request,
            String currentUserEmail) {

        User authUser = null;

        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            authUser = userRepository.findByEmail(currentUserEmail)
                    .orElse(null);
        }

        if (request.userId() != null) {
            User requestedUser = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy người dùng có ID: "
                                    + request.userId()));

            if (authUser != null) {
                boolean isStaff = authUser.getRole() != null &&
                        (authUser.getRole().toUpperCase(Locale.ROOT)
                                .contains("ADMIN")
                        || authUser.getRole().toUpperCase(Locale.ROOT)
                                .contains("RECEPTIONIST"));

                if (!isStaff &&
                        !authUser.getId().equals(requestedUser.getId())) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Bạn chỉ có thể đặt lịch cho chính mình");
                }
            }

            return requestedUser;
        }

        if (authUser != null) {
            return authUser;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "userId không được để trống");
    }
}
