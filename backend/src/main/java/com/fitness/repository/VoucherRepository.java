package com.fitness.repository;

import com.fitness.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    boolean existsByCode(String code);

    Optional<Voucher> findByCodeIgnoreCase(String code);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND v.expiryDate >= :now " +
           "AND (:scope IS NULL OR :scope = 'ALL' OR v.scope = :scope OR v.scope = 'ALL') " +
           "ORDER BY v.createdAt DESC")
    List<Voucher> findAvailableVouchers(@Param("scope") String scope, @Param("now") LocalDateTime now);

    // Tăng usedCount an toàn (atomic) khi áp dụng Voucher thành công
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 " +
           "WHERE v.id = :id AND (v.quantity IS NULL OR v.quantity <= 0 OR v.usedCount < v.quantity)")
    int incrementUsedCountIfAvailable(@Param("id") Long id);

    // Hoàn lại 1 lượt Voucher khi hủy đơn / hủy gói hoặc mã QR hết hạn
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount - 1 " +
           "WHERE UPPER(v.code) = UPPER(:code) AND v.usedCount > 0")
    int decrementUsedCount(@Param("code") String code);
}