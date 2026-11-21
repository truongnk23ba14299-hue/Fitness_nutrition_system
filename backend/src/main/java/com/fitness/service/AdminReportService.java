package com.fitness.service;

import com.fitness.dto.AdminDashboardSummaryResponse;
import com.fitness.dto.RevenueReportResponse;
import com.fitness.dto.RevenueReportResponse.RevenueDataPoint;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.Order;
import com.fitness.model.Order.OrderStatus;
import com.fitness.model.PackageEnrollment;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final PackageEnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;

    // 1. GET /api/admin/dashboard/summary (API #51)
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getDashboardSummary() {
        var allUsers = userRepository.findAll();
        long totalMembers = allUsers.stream().filter(u -> "ROLE_CLIENT".equalsIgnoreCase(u.getRole()) || "CLIENT".equalsIgnoreCase(u.getRole())).count();
        long totalTrainers = allUsers.stream().filter(u -> "ROLE_TRAINER".equalsIgnoreCase(u.getRole()) || "TRAINER".equalsIgnoreCase(u.getRole())).count();

        long activeMembers = enrollmentRepository.findByStatus(EnrollmentStatus.ACTIVE).stream()
                .filter(e -> e.getEndDate() != null && !e.getEndDate().isBefore(LocalDate.now()))
                .map(e -> e.getUser().getId())
                .distinct()
                .count();

        long totalPackages = packageRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long totalBookings = bookingRepository.count();

        // Check-in hôm nay
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        long todayCheckIns = checkInRepository.findByCheckedInAtBetween(startOfDay, endOfDay).size();

        // Doanh thu gói tập (loại trừ CANCELLED)
        var enrollments = enrollmentRepository.findAll();
        BigDecimal packageRev = enrollments.stream()
                .filter(e -> e.getStatus() != EnrollmentStatus.CANCELLED)
                .map(e -> e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Doanh thu shop (loại trừ CANCELLED)
        var orders = orderRepository.findAll();
        BigDecimal productRev = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = packageRev.add(productRev);

        // Doanh thu hôm nay
        BigDecimal todayPackageRev = enrollments.stream()
                .filter(e -> e.getStatus() != EnrollmentStatus.CANCELLED)
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                .map(e -> e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayProductRev = orders.stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayRevenue = todayPackageRev.add(todayProductRev);

        return AdminDashboardSummaryResponse.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .totalTrainers(totalTrainers)
                .totalPackages(totalPackages)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalBookings(totalBookings)
                .todayCheckIns(todayCheckIns)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .build();
    }

    // 2. GET /api/admin/reports/revenue (API #50)
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to, String groupBy) {
        LocalDate start = (from != null) ? from : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate end = (to != null) ? to : LocalDate.now();

        var enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStatus() != EnrollmentStatus.CANCELLED)
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> !e.getCreatedAt().toLocalDate().isBefore(start) && !e.getCreatedAt().toLocalDate().isAfter(end))
                .toList();

        var orders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(start) && !o.getCreatedAt().toLocalDate().isAfter(end))
                .toList();

        BigDecimal packageTotal = enrollments.stream()
                .map(e -> e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productTotal = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotal = packageTotal.add(productTotal);

        // Gom nhóm theo tháng (VD: "2026-11")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        Map<String, BigDecimal> packageByMonth = new TreeMap<>();
        Map<String, BigDecimal> productByMonth = new TreeMap<>();

        for (PackageEnrollment e : enrollments) {
            String key = e.getCreatedAt().format(formatter);
            BigDecimal val = e.getTotalPrice() != null ? e.getTotalPrice() : BigDecimal.ZERO;
            packageByMonth.merge(key, val, BigDecimal::add);
        }

        for (Order o : orders) {
            String key = o.getCreatedAt().format(formatter);
            BigDecimal val = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
            productByMonth.merge(key, val, BigDecimal::add);
        }

        Set<String> allPeriods = new TreeSet<>(packageByMonth.keySet());
        allPeriods.addAll(productByMonth.keySet());

        List<RevenueDataPoint> points = new ArrayList<>();
        for (String p : allPeriods) {
            BigDecimal pkg = packageByMonth.getOrDefault(p, BigDecimal.ZERO);
            BigDecimal prd = productByMonth.getOrDefault(p, BigDecimal.ZERO);
            points.add(new RevenueDataPoint(p, pkg, prd, pkg.add(prd)));
        }

        return RevenueReportResponse.builder()
                .totalRevenue(grandTotal)
                .packageRevenue(packageTotal)
                .productRevenue(productTotal)
                .totalEnrollments(enrollments.size())
                .totalOrders(orders.size())
                .dataPoints(points)
                .build();
    }
}