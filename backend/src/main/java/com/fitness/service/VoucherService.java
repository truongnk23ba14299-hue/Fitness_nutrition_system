package com.fitness.service;

import com.fitness.dto.ValidateVoucherRequest;
import com.fitness.dto.ValidateVoucherResponse;
import com.fitness.dto.VoucherResponse;
import com.fitness.model.Voucher;
import com.fitness.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    /**
     * 1. Lấy danh sách voucher hợp lệ (đang kích hoạt & còn hạn sử dụng) theo phạm vi (scope).
     */
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAvailableVouchers(String scope) {
        String normalizedScope = (scope != null && !scope.isBlank()) ? scope.trim().toUpperCase() : "ALL";
        LocalDateTime now = LocalDateTime.now();

        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(normalizedScope, now);
        return vouchers.stream()
                .map(VoucherResponse::from)
                .toList();
    }

    /**
     * 2. Kiểm tra tính hợp lệ của mã giảm giá và tính số tiền giảm khi thanh toán.
     */
    @Transactional(readOnly = true)
    public ValidateVoucherResponse validateVoucher(ValidateVoucherRequest req) {
        BigDecimal subtotal = req.subtotal() != null ? req.subtotal() : BigDecimal.ZERO;
        String rawCode = req.code() != null ? req.code().trim().toUpperCase() : "";

        if (rawCode.isEmpty()) {
            return ValidateVoucherResponse.invalid("Vui lòng nhập mã giảm giá.", subtotal);
        }

        // Bước 1: Tìm kiếm mã voucher trong database
        Optional<Voucher> opt = voucherRepository.findByCodeIgnoreCase(rawCode);
        if (opt.isEmpty()) {
            return ValidateVoucherResponse.invalid("Mã \"" + rawCode + "\" không tồn tại trong hệ thống.", subtotal);
        }

        Voucher voucher = opt.get();

        // Bước 2: Kiểm tra trạng thái kích hoạt
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            return ValidateVoucherResponse.invalid("Mã \"" + voucher.getCode() + "\" hiện đang tạm ngưng áp dụng.", subtotal);
        }

        LocalDateTime now = LocalDateTime.now();

        // Bước 3: Kiểm tra ngày hết hạn
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(now)) {
            return ValidateVoucherResponse.invalid("Mã \"" + voucher.getCode() + "\" đã hết hạn sử dụng.", subtotal);
        }

        // Bước 4: Kiểm tra ngày bắt đầu có hiệu lực
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
            return ValidateVoucherResponse.invalid("Mã \"" + voucher.getCode() + "\" chưa đến thời gian áp dụng.", subtotal);
        }

        // Bước 5: Kiểm tra phạm vi mặt hàng (scope: ALL, PACKAGE, PRODUCT)
        String reqScope = (req.scope() != null && !req.scope().isBlank()) ? req.scope().trim().toUpperCase() : "ALL";
        String vScope = (voucher.getScope() != null) ? voucher.getScope().trim().toUpperCase() : "ALL";

        if (!"ALL".equals(vScope) && !"ALL".equals(reqScope) && !vScope.equals(reqScope)) {
            String scopeName = "PACKAGE".equals(vScope) ? "Gói tập Hội viên" : "Thực phẩm bổ sung & Phụ kiện";
            return ValidateVoucherResponse.invalid("Mã \"" + voucher.getCode() + "\" chỉ áp dụng cho " + scopeName + ".", subtotal);
        }

        // Bước 6: Kiểm tra số lượt sử dụng còn lại (nếu có giới hạn số lượng)
        if (voucher.getQuantity() != null && voucher.getQuantity() > 0
                && voucher.getUsedCount() != null && voucher.getUsedCount() >= voucher.getQuantity()) {
            return ValidateVoucherResponse.invalid("Mã \"" + voucher.getCode() + "\" đã hết lượt sử dụng.", subtotal);
        }

        // Bước 7: Kiểm tra giá trị đơn hàng tối thiểu
        BigDecimal minOrder = voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : BigDecimal.ZERO;
        if (subtotal.compareTo(minOrder) < 0) {
            return ValidateVoucherResponse.invalid(
                    "Đơn tối thiểu " + formatVnd(minOrder) + " mới được áp dụng mã \"" + voucher.getCode() + "\".",
                    subtotal
            );
        }

        // Bước 8: Tính toán số tiền được giảm
        BigDecimal discountAmount;
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {
            BigDecimal percent = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
            discountAmount = subtotal.multiply(percent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            if (voucher.getMaxDiscount() != null && voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.min(voucher.getMaxDiscount());
            }
        } else {
            discountAmount = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
        }

        // Không cho phép số tiền giảm vượt quá tổng tiền đơn hàng
        discountAmount = discountAmount.min(subtotal);
        BigDecimal finalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        String message = String.format("Áp dụng mã %s thành công (-%s)!", voucher.getCode(), formatVnd(discountAmount));

        return ValidateVoucherResponse.success(
                message,
                discountAmount,
                finalAmount,
                VoucherResponse.from(voucher)
        );
    }

    private String formatVnd(BigDecimal val) {
        if (val == null) return "0 ₫";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(val) + " ₫";
    }

        /**
     * 3. Kiểm tra + áp dụng + trừ 1 lượt Voucher ở tầng Server khi chốt đơn / gói tập / POS
     * Trả về null nếu khách không nhập mã giảm giá.
     */
    @Transactional
    public ValidateVoucherResponse applyAndConsumeVoucher(String rawCode, BigDecimal subtotal, String scope) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }

        ValidateVoucherResponse result = validateVoucher(new ValidateVoucherRequest(rawCode, subtotal, scope));
        if (!result.valid() || result.voucher() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, result.message());
        }

        int updated = voucherRepository.incrementUsedCountIfAvailable(result.voucher().id());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Mã \"" + result.voucher().code() + "\" vừa hết lượt sử dụng!");
        }
        return result;
    }

    /**
     * 4. Hoàn lại 1 lượt Voucher khi hủy đơn / hủy gói hoặc QR hết hạn
     */
    @Transactional
    public void releaseVoucher(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return;
        }
        voucherRepository.decrementUsedCount(rawCode.trim());
    }
}
