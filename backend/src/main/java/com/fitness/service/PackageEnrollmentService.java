package com.fitness.service;

import com.fitness.config.PaymentProperties;
import com.fitness.dto.*;
import com.fitness.model.*;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PackageEnrollmentService {

    private final PackageEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final TrainerRepository trainerRepository;
    private final VoucherService voucherService;
    private final SePayPaymentService sePayPaymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentProperties paymentProperties;

    // 1. Đăng ký mua gói tập (hỗ trợ nối hạn + Voucher + VietQR SePay / CASH chờ quầy)
    @Transactional
    public EnrollmentResponse enrollPackage(String userEmail, EnrollmentRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản người dùng"));

        WorkoutPackage pkg = packageRepository.findById(request.packageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + request.packageId()));

        if (!Boolean.TRUE.equals(pkg.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gói tập này hiện đang tạm ngưng mở bán");
        }
        if (pkg.getDurationMonths() == null || pkg.getDurationMonths() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời hạn gói tập không hợp lệ");
        }

        Trainer trainer = null;
        if (request.trainerId() != null) {
            trainer = trainerRepository.findById(request.trainerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy huấn luyện viên đã chọn"));
        }

        // Xác định ngày bắt đầu: nếu còn gói ACTIVE thì tự nối ngày end_date_cu + 1
        LocalDate requestedStart = request.startDate() != null ? request.startDate() : LocalDate.now();
        LocalDate latestActiveEnd = enrollmentRepository.findByUserId(user.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE && e.getEndDate() != null)
                .map(PackageEnrollment::getEndDate)
                .filter(end -> !end.isBefore(LocalDate.now()))
                .max(Comparator.naturalOrder())
                .orElse(null);

        LocalDate start = (latestActiveEnd != null && !requestedStart.isAfter(latestActiveEnd))
                ? latestActiveEnd.plusDays(1)
                : requestedStart;
        LocalDate end = start.plusMonths(pkg.getDurationMonths());

        // Tính tiền & áp Voucher hoàn toàn tại Server (không tin số tiền FE gửi)
        BigDecimal subtotal = pkg.getPrice() != null ? pkg.getPrice() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;

        ValidateVoucherResponse voucherRes = voucherService.applyAndConsumeVoucher(
                request.voucherCode(), subtotal, "PACKAGE");
        if (voucherRes != null) {
            discountAmount = voucherRes.discountAmount();
            appliedVoucherCode = voucherRes.voucher().code();
        }
        BigDecimal finalPrice = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        // Chuẩn hóa paymentMethod: chỉ dùng VIETQR hoặc CASH (tương thích ngược TRANSFER -> VIETQR)
        String rawMethod = request.paymentMethod() != null ? request.paymentMethod().trim().toUpperCase(Locale.ROOT) : "VIETQR";
        String paymentMethod = "CASH".equals(rawMethod) ? "CASH" : "VIETQR";

        // Chỉ ACTIVE ngay nếu finalPrice == 0; còn lại đều PENDING_PAYMENT chờ thanh toán
        EnrollmentStatus initialStatus = finalPrice.compareTo(BigDecimal.ZERO) == 0
                ? EnrollmentStatus.ACTIVE
                : EnrollmentStatus.PENDING_PAYMENT;

        PackageEnrollment enrollment = PackageEnrollment.builder()
                .user(user)
                .workoutPackage(pkg)
                .trainer(trainer)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .voucherCode(appliedVoucherCode)
                .totalPrice(finalPrice)
                .paymentMethod(paymentMethod)
                .startDate(start)
                .endDate(end)
                .status(initialStatus)
                .notes(request.notes())
                .build();

        PackageEnrollment saved = enrollmentRepository.save(enrollment);

        // Nếu VIETQR và cần trả tiền -> sinh bản ghi Payment(PENDING) + ảnh qr.sepay.vn
        PaymentInfoResponse paymentInfo = null;
        if ("VIETQR".equals(paymentMethod) && finalPrice.compareTo(BigDecimal.ZERO) > 0) {
            paymentInfo = sePayPaymentService.createVietQrPayment(user, "ENROLLMENT", saved.getId(), finalPrice, null);
        }

        return toResponse(saved, paymentInfo);
    }

    // 2. Lấy danh sách gói tập mà người dùng đã đăng ký
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản người dùng"));

        return enrollmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(e -> toResponse(e, findLatestPaymentInfo(e.getId())))
                .toList();
    }

    // 3. Admin: Báo cáo doanh thu & tìm kiếm lượt mua (CHỈ tính ACTIVE & EXPIRED, loại PENDING_PAYMENT & CANCELLED)
    @Transactional(readOnly = true)
    public AdminEnrollmentReportResponse getEnrollmentReport(String keyword) {
        List<PackageEnrollment> list = enrollmentRepository.searchEnrollments(keyword);

        BigDecimal totalRevenue = list.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.EXPIRED)
                .map(e -> e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeCount = list.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
        long expiredCount = list.stream().filter(e -> e.getStatus() == EnrollmentStatus.EXPIRED).count();
        long cancelledCount = list.stream().filter(e -> e.getStatus() == EnrollmentStatus.CANCELLED).count();

        List<EnrollmentResponse> dtoList = list.stream()
                .map(e -> toResponse(e, null))
                .toList();

        return new AdminEnrollmentReportResponse(
                totalRevenue,
                list.size(),
                activeCount,
                expiredCount,
                cancelledCount,
                dtoList
        );
    }

    // 4. Admin / Lễ tân: Cập nhật trạng thái gói tập (hỗ trợ xác nhận thu tiền quầy PENDING_PAYMENT -> ACTIVE)
    @Transactional
    public EnrollmentResponse updateStatus(Long id, EnrollmentStatus newStatus) {
        PackageEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy lượt đăng ký gói tập với ID: " + id));

        EnrollmentStatus oldStatus = enrollment.getStatus();
        enrollment.setStatus(newStatus);
        PackageEnrollment saved = enrollmentRepository.save(enrollment);

        // Khi xác nhận thu tiền tại quầy: PENDING_PAYMENT -> ACTIVE => ghi nhận PAID vào bảng payments
        if (oldStatus == EnrollmentStatus.PENDING_PAYMENT && newStatus == EnrollmentStatus.ACTIVE) {
            var existingOpt = findLatestPaymentEntity(saved.getId());
            if (existingOpt.isPresent()) {
                Payment p = existingOpt.get();
                if (!"PAID".equals(p.getStatus())) {
                    p.setStatus("PAID");
                    p.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(p);
                }
            } else {
                String method = saved.getPaymentMethod() != null ? saved.getPaymentMethod() : "CASH";
                sePayPaymentService.createImmediatePaidPayment(
                        saved.getUser(), "ENROLLMENT", saved.getId(), saved.getTotalPrice(), method, null);
            }
        }

        // Khi hủy hợp đồng đang chờ thanh toán => hủy Payment PENDING & hoàn lượt Voucher
        if (oldStatus == EnrollmentStatus.PENDING_PAYMENT && newStatus == EnrollmentStatus.CANCELLED) {
            findLatestPaymentEntity(saved.getId())
                    .ifPresent(p -> {
                        if ("PENDING".equals(p.getStatus())) {
                            p.setStatus("CANCELLED");
                            paymentRepository.save(p);
                        }
                    });
            voucherService.releaseVoucher(saved.getVoucherCode());
        }

        return toResponse(saved, findLatestPaymentInfo(saved.getId()));
    }

    private Optional<Payment> findLatestPaymentEntity(Long enrollmentId) {
        return paymentRepository.findTopByRefTypeAndRefIdOrderByCreatedAtDesc("ENROLLMENT", enrollmentId)
                .or(() -> paymentRepository.findTopByRefTypeAndRefIdOrderByCreatedAtDesc("POS", enrollmentId));
    }

    private PaymentInfoResponse findLatestPaymentInfo(Long enrollmentId) {
        return findLatestPaymentEntity(enrollmentId)
                .map(p -> PaymentInfoResponse.from(p, paymentProperties.getSepay().getBankName()))
                .orElse(null);
    }

    private EnrollmentResponse toResponse(PackageEnrollment e, PaymentInfoResponse paymentInfo) {
        return new EnrollmentResponse(
                e.getId(),
                e.getWorkoutPackage().getId(),
                e.getWorkoutPackage().getTitle(),
                e.getUser().getFullName(),
                e.getUser().getEmail(),
                e.getTotalPrice(),
                e.getStartDate(),
                e.getEndDate(),
                e.getStatus(),
                e.getNotes(),
                e.getCreatedAt(),
                e.getSubtotal(),
                e.getDiscountAmount(),
                e.getVoucherCode(),
                e.getPaymentMethod(),
                paymentInfo
        );
    }
}