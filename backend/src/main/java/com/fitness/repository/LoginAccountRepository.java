package com.usth.fitness_project.repository;

import com.usth.fitness_project.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginAccountRepository extends JpaRepository<Account, Long> {

    /**
     * Tìm kiếm tài khoản dựa trên email.
     */
    Optional<Account> findByEmail(String email);

    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống chưa.
     */
    boolean existsByEmail(String email);
}
