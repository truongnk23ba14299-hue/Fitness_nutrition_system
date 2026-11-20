package com.fitness.service;

import com.fitness.dto.PaymentHistoryResponse;
import com.fitness.model.Order;
import com.fitness.model.PackageEnrollment;
import com.fitness.model.User;
import com.fitness.repository.OrderRepository;
import com.fitness.repository.PackageEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PackageEnrollmentRepository packageEnrollmentRepository;
    private final OrderRepository orderRepository;

    /**
     * Lấy lịch sử hóa đơn thanh toán tập trung của học viên (cả Gói tập và Đơn hàng shop)
     */
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getMyPaymentHistory(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        List<PaymentHistoryResponse> result = new ArrayList<>();

        // 1. Lấy danh sách đăng ký gói tập của học viên
        List<PackageEnrollment> enrollments = packageEnrollmentRepository.findMyEnrollmentsWithPackage(user.getId());
        for (PackageEnrollment e : enrollments) {
            BigDecimal totalPrice = e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal subtotal = e.getSubtotal() != null ? e.getSubtotal() : totalPrice;
            BigDecimal discount = e.getDiscountAmount() != null ? e.getDiscountAmount() : BigDecimal.ZERO;
            String pkgTitle = (e.getWorkoutPackage() != null && e.getWorkoutPackage().getTitle() != null)
                    ? e.getWorkoutPackage().getTitle()
                    : "Gói tập Hội viên";

            LocalDateTime paymentDate = e.getCreatedAt() != null
                    ? e.getCreatedAt()
                    : (e.getStartDate() != null ? e.getStartDate().atStartOfDay() : LocalDateTime.now());

            PaymentHistoryResponse.PaymentItemDto item = PaymentHistoryResponse.PaymentItemDto.builder()
                    .name(pkgTitle)
                    .quantity(1)
                    .unitPrice(subtotal)
                    .total(subtotal)
                    .build();

            PaymentHistoryResponse history = PaymentHistoryResponse.builder()
                    .id("HD-" + e.getId())
                    .invoiceNo("INV-PKG-" + e.getId())
                    .refType("ENROLLMENT")
                    .description("Đăng ký " + pkgTitle)
                    .amount(totalPrice)
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .voucherCode(e.getVoucherCode())
                    .paymentMethod(e.getPaymentMethod() != null ? e.getPaymentMethod() : "VIETQR")
                    .status(e.getStatus() != null ? e.getStatus().name() : "PAID")
                    .date(paymentDate)
                    .items(List.of(item))
                    .build();

            result.add(history);
        }

        // 2. Lấy danh sách đơn hàng mua sản phẩm TPCN của học viên
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (Order o : orders) {
            BigDecimal totalAmount = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal subtotal = o.getSubtotal() != null ? o.getSubtotal() : totalAmount;
            BigDecimal discount = o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO;

            List<PaymentHistoryResponse.PaymentItemDto> items = new ArrayList<>();
            if (o.getItems() != null) {
                o.getItems().forEach(it -> {
                    items.add(PaymentHistoryResponse.PaymentItemDto.builder()
                            .name(it.getProductName())
                            .quantity(it.getQuantity())
                            .unitPrice(it.getUnitPrice())
                            .total(it.getSubTotal())
                            .build());
                });
            }

            int itemCount = items.isEmpty() ? 1 : items.size();
            LocalDateTime orderDate = o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now();

            PaymentHistoryResponse history = PaymentHistoryResponse.builder()
                    .id("DH-" + o.getId())
                    .invoiceNo("INV-ORD-" + o.getId())
                    .refType("ORDER")
                    .description("Đơn hàng TPCN #" + o.getId() + " (" + itemCount + " sản phẩm)")
                    .amount(totalAmount)
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .voucherCode(o.getVoucherCode())
                    .paymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod() : "TRANSFER")
                    .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : "PAID")
                    .date(orderDate)
                    .items(items)
                    .build();

            result.add(history);
        }

        // 3. Sắp xếp toàn bộ hóa đơn theo thời gian mới nhất lên đầu
        result.sort((a, b) -> {
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });

        return result;
    }
}
