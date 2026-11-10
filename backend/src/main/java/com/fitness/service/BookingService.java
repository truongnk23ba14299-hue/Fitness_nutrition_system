package com.fitness.service;

import com.fitness.dto.BookPtRequest;
import com.fitness.dto.BookPtResponse;
import com.fitness.model.Booking;
import com.fitness.model.Trainer;
import com.fitness.repository.BookingRepository;
import com.fitness.repository.TrainerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {

    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "CONFIRMED");

    private final BookingRepository bookingRepository;
    private final TrainerRepository trainerRepository;

    public BookingService(BookingRepository bookingRepository, TrainerRepository trainerRepository) {
        this.bookingRepository = bookingRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public BookPtResponse createPtBooking(BookPtRequest request) {
        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy huấn luyện viên"));

        if (!Boolean.TRUE.equals(trainer.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Huấn luyện viên hiện không hoạt động");
        }

        boolean slotTaken = bookingRepository.existsByTrainer_IdAndBookingDateTimeAndStatusIn(
                trainer.getId(), request.bookingDateTime(), ACTIVE_STATUSES);
        if (slotTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Khung giờ này đã được đặt. Vui lòng chọn giờ khác");
        }

        Booking booking = new Booking();
        booking.setUserId(request.userId());
        booking.setTrainer(trainer);
        booking.setBookingDateTime(request.bookingDateTime());
        booking.setNote(request.note());
        booking.setStatus("PENDING");

        return BookPtResponse.fromEntity(bookingRepository.save(booking));
    }
}
