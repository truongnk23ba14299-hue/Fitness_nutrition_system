package com.fitness.service;

import com.fitness.config.PaymentProperties;
import com.fitness.dto.PaymentHistoryResponse;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.Order;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.Payment;
import com.fitness.model.User;
import com.fitness.repository.OrderRepository;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PackageEnrollmentRepository packageEnrollmentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProperties paymentProperties;

    /**
     * Lấy lịch sử hóa đơn thanh toán tập trung của học viên:
     * ưu tiên đọc từ bảng payments (có qrUrl, expiresAt, paymentCode),
     * đồng thời giữ tương thích cho các gói CASH chờ quầy / đơn COD chưa phát sinh dòng trong payments.
     */
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getMyPaymentHistory(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        List<PackageEnrollment> enrollments = packageEnrollmentRepository.findMyEnrollmentsWithPackage(user.getId());
        Map<Long, PackageEnrollment> enrollmentMap = new LinkedHashMap<>();
        for (PackageEnrollment e : enrollments) {
            enrollmentMap.put(e.getId(), e);
        }

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Map<Long, Order> orderMap = new LinkedHashMap<>();
        for (Order o : orders) {
            orderMap.put(o.getId(), o);
        }

        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Set<Long> seenEnrollmentIds = new HashSet<>();
        Set<Long> seenOrderIds = new HashSet<>();
        List<PaymentHistoryResponse> result = new ArrayList<>();
        String bankName = paymentProperties.getSepay().getBankName();
        LocalDateTime now = LocalDateTime.now();

        // 1. Đọc trực tiếp từ bảng payments
        for (Payment p : payments) {
            String effectiveStatus = resolveEffectivePaymentStatus(p, now);
            LocalDateTime paymentDate = p.getPaidAt() != null ? p.getPaidAt() : p.getCreatedAt();
            BigDecimal amount = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;

            String displayId;
            String description;
            BigDecimal subtotal = amount;
            BigDecimal discount = BigDecimal.ZERO;
            String voucherCode = null;
            List<PaymentHistoryResponse.PaymentItemDto> items;

            if ("ENROLLMENT".equalsIgnoreCase(p.getRefType()) || "POS".equalsIgnoreCase(p.getRefType())) {
                PackageEnrollment e = p.getRefId() != null ? enrollmentMap.get(p.getRefId()) : null;
                if (p.getRefId() != null) {
                    seenEnrollmentIds.add(p.getRefId());
                }
                String prefix = "POS".equalsIgnoreCase(p.getRefType()) ? "POS-" : "HD-";
                displayId = prefix + (p.getRefId() != null ? p.getRefId() : p.getId());

                if (e != null) {
                    String pkgTitle = (e.getWorkoutPackage() != null && e.getWorkoutPackage().getTitle() != null)
                            ? e.getWorkoutPackage().getTitle()
                            : "Gói tập Hội viên";
                    description = "Đăng ký " + pkgTitle;
                    subtotal = e.getSubtotal() != null ? e.getSubtotal() : amount;
                    discount = e.getDiscountAmount() != null ? e.getDiscountAmount() : BigDecimal.ZERO;
                    voucherCode = e.getVoucherCode();
                    items = List.of(PaymentHistoryResponse.PaymentItemDto.builder()
                            .name(pkgTitle)
                            .quantity(1)
                            .unitPrice(subtotal)
                            .total(subtotal)
                            .build());
                } else {
                    description = "Thanh toán gói tập #" + (p.getRefId() != null ? p.getRefId() : p.getId());
                    items = List.of(PaymentHistoryResponse.PaymentItemDto.builder()
                            .name(description)
                            .quantity(1)
                            .unitPrice(amount)
                            .total(amount)
                            .build());
                }
            } else if ("ORDER".equalsIgnoreCase(p.getRefType())) {
                Order o = p.getRefId() != null ? orderMap.get(p.getRefId()) : null;
                if (p.getRefId() != null) {
                    seenOrderIds.add(p.getRefId());
                }
                displayId = "DH-" + (p.getRefId() != null ? p.getRefId() : p.getId());

                if (o != null) {
                    items = mapOrderItems(o);
                    int itemCount = items.isEmpty() ? 1 : items.size();
                    description = "Đơn hàng TPCN #" + o.getId() + " (" + itemCount + " sản phẩm)";
                    subtotal = o.getSubtotal() != null ? o.getSubtotal() : amount;
                    discount = o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO;
                    voucherCode = o.getVoucherCode();
                } else {
                    description = "Đơn hàng TPCN #" + (p.getRefId() != null ? p.getRefId() : p.getId());
                    items = List.of();
                }
            } else {
                displayId = "PAY-" + p.getId();
                description = "Giao dịch thanh toán #" + p.getId();
                items = List.of();
            }

            result.add(PaymentHistoryResponse.builder()
                    .id(displayId)
                    .invoiceNo(p.getInvoiceNo())
                    .refType(p.getRefType())
                    .description(description)
                    .amount(amount)
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .voucherCode(voucherCode)
                    .paymentMethod(p.getMethod())
                    .status(effectiveStatus)
                    .date(paymentDate)
                    .items(items)
                    .paymentId(p.getId())
                    .paymentCode(p.getPaymentCode())
                    .paymentStatus(effectiveStatus)
                    .qrUrl(p.getQrUrl())
                    .bankCode(p.getBankCode())
                    .bankName(bankName)
                    .accountNumber(p.getAccountNumber())
                    .accountName(p.getAccountName())
                    .expiresAt(p.getExpiresAt())
                    .paidAt(p.getPaidAt())
                    .build());
        }

        // 2. Tương thích ngược: gói tập chưa có dòng trong payments (VD: CASH chờ thu tại quầy hoặc dữ liệu cũ)
        for (PackageEnrollment e : enrollments) {
            if (seenEnrollmentIds.contains(e.getId())) continue;

            BigDecimal totalPrice = e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal subtotal = e.getSubtotal() != null ? e.getSubtotal() : totalPrice;
            BigDecimal discount = e.getDiscountAmount() != null ? e.getDiscountAmount() : BigDecimal.ZERO;
            String pkgTitle = (e.getWorkoutPackage() != null && e.getWorkoutPackage().getTitle() != null)
                    ? e.getWorkoutPackage().getTitle()
                    : "Gói tập Hội viên";

            LocalDateTime paymentDate = e.getCreatedAt() != null
                    ? e.getCreatedAt()
                    : (e.getStartDate() != null ? e.getStartDate().atStartOfDay() : LocalDateTime.now());

            String fallbackPaymentStatus = e.getStatus() == EnrollmentStatus.PENDING_PAYMENT
                    ? "PENDING_PAYMENT"
                    : (e.getStatus() == EnrollmentStatus.CANCELLED ? "CANCELLED" : "PAID");

            result.add(PaymentHistoryResponse.builder()
                    .id("HD-" + e.getId())
                    .invoiceNo("INV-PKG-" + e.getId())
                    .refType("ENROLLMENT")
                    .description("Đăng ký " + pkgTitle)
                    .amount(totalPrice)
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .voucherCode(e.getVoucherCode())
                    .paymentMethod(e.getPaymentMethod() != null ? e.getPaymentMethod() : "CASH")
                    .status(e.getStatus() != null ? e.getStatus().name() : fallbackPaymentStatus)
                    .paymentStatus(fallbackPaymentStatus)
                    .date(paymentDate)
                    .items(List.of(PaymentHistoryResponse.PaymentItemDto.builder()
                            .name(pkgTitle)
                            .quantity(1)
                            .unitPrice(subtotal)
                            .total(subtotal)
                            .build()))
                    .build());
        }

        // 3. Tương thích ngược: đơn hàng COD chưa hoàn tất (chưa tạo dòng PAID trong payments) hoặc dữ liệu cũ
        for (Order o : orders) {
            if (seenOrderIds.contains(o.getId())) continue;

            BigDecimal totalAmount = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal subtotal = o.getSubtotal() != null ? o.getSubtotal() : totalAmount;
            BigDecimal discount = o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO;
            List<PaymentHistoryResponse.PaymentItemDto> items = mapOrderItems(o);
            int itemCount = items.isEmpty() ? 1 : items.size();
            LocalDateTime orderDate = o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now();

            String fallbackPaymentStatus = o.getPaymentStatus() != null
                    ? o.getPaymentStatus().name()
                    : (o.getOrderStatus() == Order.OrderStatus.COMPLETED ? "PAID" : "UNPAID");

            result.add(PaymentHistoryResponse.builder()
                    .id("DH-" + o.getId())
                    .invoiceNo("INV-ORD-" + o.getId())
                    .refType("ORDER")
                    .description("Đơn hàng TPCN #" + o.getId() + " (" + itemCount + " sản phẩm)")
                    .amount(totalAmount)
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .voucherCode(o.getVoucherCode())
                    .paymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod() : "COD")
                    .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : fallbackPaymentStatus)
                    .paymentStatus(fallbackPaymentStatus)
                    .date(orderDate)
                    .items(items)
                    .build());
        }

        // 4. Sắp xếp toàn bộ hóa đơn theo thời gian mới nhất lên đầu
        result.sort((a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });

        return result;
    }

    private String resolveEffectivePaymentStatus(Payment p, LocalDateTime now) {
        if ("PENDING".equalsIgnoreCase(p.getStatus())
                && p.getExpiresAt() != null
                && p.getExpiresAt().isBefore(now)) {
            return "EXPIRED";
        }
        return p.getStatus();
    }

    private List<PaymentHistoryResponse.PaymentItemDto> mapOrderItems(Order o) {
        List<PaymentHistoryResponse.PaymentItemDto> items = new ArrayList<>();
        if (o.getItems() != null) {
            o.getItems().forEach(it -> items.add(PaymentHistoryResponse.PaymentItemDto.builder()
                    .name(it.getProductName())
                    .quantity(it.getQuantity())
                    .unitPrice(it.getUnitPrice())
                    .total(it.getSubTotal())
                    .build()));
        }
        return items;
    }
}
