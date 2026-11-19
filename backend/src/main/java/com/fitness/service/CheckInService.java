package com.fitness.service;

import com.fitness.dto.CheckInRequest;
import com.fitness.dto.CheckInResponse;
import com.fitness.model.CheckIn;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.User;
import com.fitness.repository.CheckInRepository;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final PackageEnrollmentRepository packageEnrollmentRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    /**
     * POST /api/checkins
     * Điểm danh học viên qua QR. Kiểm tra chống quét trùng < 2 giờ trả 409 CONFLICT.
     */
    @Transactional
    public CheckInResponse recordCheckIn(CheckInRequest request, User staff) {

        User member = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy học viên với ID: " + request.userId()
                ));


        if ("LOCKED".equalsIgnoreCase(member.getStatus()) || "INACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản học viên (" + member.getFullName() + ") đang bị khóa hoặc ngưng hoạt động!"
            );
        }

        // . QUY TẮC CHỐNG QUÉT TRÙNG TRONG VÒNG 2 GIỜ -> TRẢ HTTP 409 CONFLICT
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        Optional<CheckIn> recentCheckIn = checkInRepository
                .findTopByUserIdAndCheckedInAtAfterOrderByCheckedInAtDesc(member.getId(), twoHoursAgo);

        if (recentCheckIn.isPresent()) {
            CheckIn prev = recentCheckIn.get();
            String checkInTimeStr = prev.getCheckedInAt().format(TIME_FORMATTER);
            log.warn(">>> [CHECKIN 409] Học viên {} ({}) quét trùng lặp lúc {}",
                    member.getFullName(), member.getId(), checkInTimeStr);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Học viên " + member.getFullName() + " vừa quét thẻ lúc " + checkInTimeStr + " (Chưa quá 2 giờ)!"
            );
        }

        PackageEnrollment enrollment = null;
        if (request.enrollmentId() != null) {
            enrollment = packageEnrollmentRepository.findById(request.enrollmentId()).orElse(null);
        }

  
        if (enrollment == null) {
            List<PackageEnrollment> enrollments = packageEnrollmentRepository.findByUserId(member.getId());
            LocalDate today = LocalDate.now();
            enrollment = enrollments.stream()
                    .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                    .filter(e -> e.getEndDate() != null && !e.getEndDate().isBefore(today))
                    .findFirst()
                    .orElse(null);
        }


        String method = (request.method() != null && !request.method().isBlank())
                ? request.method().trim().toUpperCase()
                : "QR";

        CheckIn checkIn = CheckIn.builder()
                .user(member)
                .enrollment(enrollment)
                .method(method)
                .staff(staff)
                .checkedInAt(LocalDateTime.now())
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        log.info(">>> [CHECKIN THÀNH CÔNG] Học viên {} (ID: {}) check-in qua {} bởi nhân viên {}",
                member.getFullName(), member.getId(), method, staff != null ? staff.getFullName() : "Hệ thống");

        return CheckInResponse.fromEntity(saved);
    }

     /**
     * GET /api/checkins?date=YYYY-MM-DD&limit=N
     * 1. Nếu có date: Lấy danh sách điểm danh của ngày đó (phục vụ đếm tổng khách hôm nay)
     * 2. Nếu không có date: Lấy N lượt check-in gần nhất (phục vụ Dashboard)
     */
    @Transactional(readOnly = true)
    public List<CheckInResponse> getCheckIns(LocalDate date, Integer limit) {
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            List<CheckIn> list = checkInRepository.findByCheckedInAtBetween(start, end);

            // Nếu người dùng có truyền thêm limit thì cắt theo limit, ngược lại trả về toàn bộ khách trong ngày
            if (limit != null && limit > 0 && list.size() > limit) {
                list = list.subList(0, limit);
            }

            return list.stream()
                    .map(CheckInResponse::fromEntity)
                    .toList();
        }

        // Mặc định nếu không truyền gì thì lấy 10 lượt gần nhất
        int pageSize = (limit != null && limit > 0) ? limit : 10;
        return checkInRepository.findRecentCheckIns(PageRequest.of(0, pageSize))
                .stream()
                .map(CheckInResponse::fromEntity)
                .toList();
    }
}