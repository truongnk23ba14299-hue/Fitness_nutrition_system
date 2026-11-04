package com.fitness.repository;

import com.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm kiếm người dùng dựa trên email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra xem email đã được đăng ký trong hệ thống chưa.
     */
    boolean existsByEmail(String email);
}
