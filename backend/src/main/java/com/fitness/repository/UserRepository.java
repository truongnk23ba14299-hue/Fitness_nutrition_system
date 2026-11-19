package com.fitness.repository;

import com.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Tra cứu hội viên (ROLE_CLIENT) theo từ khóa
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:searchId IS NOT NULL AND u.id = :searchId) OR (" +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "AND (u.role IS NULL OR u.role = 'ROLE_CLIENT' OR u.role = 'CLIENT')) " +
           "ORDER BY u.id DESC")
    List<User> searchMembers(@Param("keyword") String keyword, @Param("searchId") Long searchId);
}
