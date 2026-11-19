package com.fitness.service;

import com.fitness.dto.CreateVoucherRequest;
import com.fitness.dto.UpdateVoucherRequest;
import com.fitness.dto.VoucherResponse;
import com.fitness.model.Voucher;
import com.fitness.repository.VoucherRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVoucherService {
    private final VoucherRepository voucherRepository;

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers(){
        return voucherRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
                .stream()
                .map(VoucherResponse::from)
                .toList();
    }

    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest req){
        String code = req.code().trim().toUpperCase();
        validateVoucher(req,code);
        Voucher voucher = buildVoucher(req, code);
        return VoucherResponse.from(voucherRepository.save(voucher));
    }

    @Transactional
    public VoucherResponse updateVoucher(Long id, @Valid UpdateVoucherRequest req){
        Voucher voucher = voucherRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND
                ,"Voucher not found with " +id));

        validateDiscountAndDates(req.discountType(),req.discountValue()
                                 ,req.startedDate(), req.expiryDate());

        if (req.quantity() < voucher.getUsedCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity cannot be less than used count (" + voucher.getUsedCount() + ")");
        }

        applyUpdate(voucher, req);
        return VoucherResponse.from(voucherRepository.save(voucher));
    }

    @Transactional
    public void deleteVoucher(Long id){
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found with " +id));
        if (voucher.getUsedCount() > 0){
            voucher.setIsActive(false);
        }
        else {
            voucherRepository.delete(voucher);
        }
    }

    private void validateDiscountAndDates(Voucher.DiscountType discountType,
                                          BigDecimal discountValue,
                                          LocalDateTime startedDate,
                                          LocalDateTime expiryDate) {
        if (!expiryDate.isAfter(startedDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expiry date must be after start date");
        }

        if (discountType == Voucher.DiscountType.PERCENT
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100");
        }
    }

    private void validateVoucher(CreateVoucherRequest req, String code) {
        validateDiscountAndDates(req.discountType(), req.discountValue(),
                req.startedDate(), req.expiryDate());

        if (voucherRepository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Voucher code already exists: " + code);
        }
    }

    private Voucher buildVoucher(CreateVoucherRequest req, String code) {
        boolean isPercent = req.discountType() == Voucher.DiscountType.PERCENT;

        return Voucher.builder()
                .code(code)
                .title(req.title().trim())
                .discountType(req.discountType())
                .discountValue(req.discountValue())
                .maxDiscount(isPercent ? req.maxDiscount() : null)
                .minOrderValue(req.minOrderValue())
                .quantity(req.quantity())
                .usedCount(0)
                .scope(req.scope() != null ? req.scope() : "ALL")
                .isActive(req.isActive() == null || req.isActive())
                .startDate(req.startedDate())
                .expiryDate(req.expiryDate())
                .build();
    }

    private void applyUpdate(Voucher voucher, @Valid UpdateVoucherRequest req) {
        boolean isPercent =  req.discountType() == Voucher.DiscountType.PERCENT;

        voucher.setTitle(req.title().trim());
        voucher.setDiscountType(req.discountType());
        voucher.setDiscountValue(req.discountValue());
        voucher.setMaxDiscount(isPercent ? req.maxDiscount() : null);
        voucher.setMinOrderValue(req.minOrderValue());
        voucher.setQuantity(req.quantity());
        voucher.setScope(req.scope() != null ? req.scope() : "ALL");
        voucher.setIsActive(req.isActive() == null || req.isActive());
        voucher.setStartDate(req.startedDate());
        voucher.setExpiryDate(req.expiryDate());
    }
}

