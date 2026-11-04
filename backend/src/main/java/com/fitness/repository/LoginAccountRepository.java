package com.fitness.repository;

import com.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginAccountRepository extends JpaRepository<User, Long> {

    /**
     * Tìm kiếm tài khoản dựa trên email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống chưa.
     */
    boolean existsByEmail(String email);
}
