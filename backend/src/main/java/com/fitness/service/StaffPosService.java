package com.fitness.service;

import com.fitness.dto.PaymentInfoResponse;
import com.fitness.dto.PosSaleRequest;
import com.fitness.dto.PosSaleResponse;
import com.fitness.model.*;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffPosService {

    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final PackageEnrollmentRepository packageEnrollmentRepository;
    private final CheckInRepository checkInRepository;
    private final PasswordEncoder passwordEncoder;
    private final VoucherService voucherService;
    private final SePayPaymentService sePayPaymentService;

    @Transactional
    public PosSaleResponse processPosSale(String staffEmail, PosSaleRequest request) {
        // 1. Xác định nhân viên lễ tân lập phiếu
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin lễ tân!"));

        // 2. Tìm hoặc tạo mới tài khoản hội viên
        User customer = resolveOrCreateCustomer(request);

        // 3. Tìm gói tập theo ID
        WorkoutPackage pkg = packageRepository.findById(request.packageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy gói tập với ID: " + request.packageId()));

        if (!Boolean.TRUE.equals(pkg.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gói tập này hiện đang tạm ngưng mở bán!");
        }

        // 4. Tính toán thời hạn sử dụng
        LocalDate start = LocalDate.now();
        LocalDate end;
        if (pkg.getId() == 1L || (pkg.getDurationMonths() != null && pkg.getDurationMonths() == 0)) {
            end = start;
        } else {
            int months = (pkg.getDurationMonths() != null && pkg.getDurationMonths() > 0) ? pkg.getDurationMonths() : 1;
            end = start.plusMonths(months);
        }

        // 5. Tính toán tài chính & áp Voucher tại Server (KHÔNG tin discountAmount/totalPrice từ FE)
        BigDecimal subtotal = pkg.getPrice() != null ? pkg.getPrice() : BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        String appliedVoucherCode = null;

        var voucherRes = voucherService.applyAndConsumeVoucher(request.voucherCode(), subtotal, "PACKAGE");
        if (voucherRes != null) {
            discount = voucherRes.discountAmount();
            appliedVoucherCode = voucherRes.voucher().code();
        }
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);

        String rawMethod = (request.paymentMethod() != null && !request.paymentMethod().isBlank())
                ? request.paymentMethod().trim().toUpperCase(Locale.ROOT)
                : "CASH";
        String paymentMethod = "CASH".equals(rawMethod) ? "CASH" : "VIETQR";
        boolean isVietQrPending = "VIETQR".equals(paymentMethod) && total.compareTo(BigDecimal.ZERO) > 0;

        // 6. Lưu hợp đồng gói tập (PackageEnrollment)
        PackageEnrollment enrollment = PackageEnrollment.builder()
                .user(customer)
                .workoutPackage(pkg)
                .totalPrice(total)
                .subtotal(subtotal)
                .discountAmount(discount)
                .voucherCode(appliedVoucherCode)
                .paymentMethod(paymentMethod)
                .startDate(start)
                .endDate(end)
                .status(isVietQrPending ? EnrollmentStatus.PENDING_PAYMENT : EnrollmentStatus.ACTIVE)
                .notes(request.notes() != null && !request.notes().isBlank() ? request.notes() : "Đăng ký tại quầy lễ tân (POS)")
                .build();

        PackageEnrollment savedEnrollment = packageEnrollmentRepository.save(enrollment);

        // 7. Ghi nhận thanh toán vào bảng payments
        PaymentInfoResponse paymentInfo = null;
        String invoiceCode;
        if (isVietQrPending) {
            paymentInfo = sePayPaymentService.createVietQrPayment(customer, "POS", savedEnrollment.getId(), total, staff);
            invoiceCode = paymentInfo.invoiceNo();
        } else {
            Payment paid = sePayPaymentService.createImmediatePaidPayment(
                    customer, "POS", savedEnrollment.getId(), total, paymentMethod, staff);
            invoiceCode = paid.getInvoiceNo();
        }

        // 8. Chỉ tự động check-in khi đã thu tiền ngay (ACTIVE)
        boolean isCheckedIn = false;
        if (!isVietQrPending && (Boolean.TRUE.equals(request.autoCheckIn()) || pkg.getId() == 1L)) {
            CheckIn checkIn = CheckIn.builder()
                    .user(customer)
                    .method("MANUAL")
                    .checkedInAt(LocalDateTime.now())
                    .build();
            checkInRepository.save(checkIn);
            isCheckedIn = true;
        }

        String memberCode = String.format("FL-%05d", customer.getId());

        return PosSaleResponse.builder()
                .invoiceCode(invoiceCode)
                .enrollmentId(savedEnrollment.getId())
                .memberId(customer.getId())
                .memberCode(memberCode)
                .customerName(customer.getFullName())
                .customerPhone(customer.getPhone())
                .customerEmail(customer.getEmail())
                .packageId(pkg.getId())
                .packageTitle(pkg.getTitle())
                .subtotal(subtotal)
                .discountAmount(discount)
                .voucherCode(appliedVoucherCode)
                .totalAmount(total)
                .paymentMethod(paymentMethod)
                .startDate(start)
                .endDate(end)
                .status(savedEnrollment.getStatus())
                .notes(savedEnrollment.getNotes())
                .staffName(staff.getFullName())
                .isCheckedIn(isCheckedIn)
                .createdAt(savedEnrollment.getCreatedAt() != null ? savedEnrollment.getCreatedAt() : LocalDateTime.now())
                .payment(paymentInfo)
                .build();
    }

    /**
     * Tự động nhận diện khách hàng:
     * - Tìm theo mã FL-00012
     * - Tìm theo số điện thoại
     * - Hoặc tự tạo User mới cho khách vãng lai
     */
    private User resolveOrCreateCustomer(PosSaleRequest request) {
        // 1. Thử tìm theo memberCode (FL-00012 hoặc 12)
        if (request.memberCode() != null && !request.memberCode().isBlank()) {
            String raw = request.memberCode().trim().replaceAll("[^0-9]", "");
            if (!raw.isBlank()) {
                try {
                    Long searchId = Long.parseLong(raw);
                    var userOpt = userRepository.findById(searchId);
                    if (userOpt.isPresent()) return userOpt.get();
                } catch (NumberFormatException ignored) {}
            }
        }

        // 2. Thử tìm theo số điện thoại trong hệ thống
        String cleanPhone = request.customerPhone().trim().replaceAll("[^0-9+]", "");
        var existingUsers = userRepository.findAll();
        for (User u : existingUsers) {
            if (u.getPhone() != null && u.getPhone().replaceAll("[^0-9+]", "").equals(cleanPhone)) {
                return u;
            }
        }

        // 3. Nếu chưa có tài khoản: Tạo ngay tài khoản Hội viên mới cho khách vãng lai
        String email = (request.customerEmail() != null && !request.customerEmail().isBlank())
                ? request.customerEmail().trim().toLowerCase()
                : "guest_" + cleanPhone + "@fitlife.local";

        // Tránh trùng lặp email nếu email sinh tự động đã có
        if (userRepository.existsByEmail(email)) {
            email = "guest_" + cleanPhone + "_" + System.currentTimeMillis() % 1000 + "@fitlife.local";
        }

        User newUser = User.builder()
                .fullName(request.customerName().trim())
                .phone(cleanPhone)
                .email(email)
                .password(passwordEncoder.encode("123456")) // Mật khẩu mặc định
                .role("ROLE_CLIENT")
                .status("ACTIVE")
                .build();

        return userRepository.save(newUser);
    }
        public java.util.Map<String, Object> getTodayPosSummary() {
        // Thống kê nhanh cho Dashboard Lễ tân
        return java.util.Map.of(
            "message", "Thống kê doanh số bán hàng tại quầy POS trong ngày",
            "date", java.time.LocalDate.now().toString(),
            "todaySalesCount", checkInRepository.count(), // hoặc đếm từ packageEnrollments POS
            "status", "ACTIVE"
        );
    }
}