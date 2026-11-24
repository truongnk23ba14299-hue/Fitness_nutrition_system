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

    // 1. Tìm user theo Email
    Optional<User> findByEmail(String email);

    // 2. Tìm user theo Email không phân biệt hoa thường
    Optional<User> findByEmailIgnoreCase(String email);

    // 3. Tìm user theo Username không phân biệt hoa thường
    Optional<User> findByUsernameIgnoreCase(String username);

    // 4. Tìm kiếm linh hoạt: khớp Email HOẶC khớp Username (Dành cho chức năng Đăng nhập)
    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    // 5. Tìm user theo mã định danh Google ID
    Optional<User> findByGoogleId(String googleId);

    // 6. Kiểm tra xem Email đã tồn tại chưa
    boolean existsByEmail(String email);

    // 7. Kiểm tra xem Username đã có người dùng chưa
    boolean existsByUsernameIgnoreCase(String username);

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