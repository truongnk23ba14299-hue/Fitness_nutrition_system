package com.fitness.repository;

import com.fitness.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
