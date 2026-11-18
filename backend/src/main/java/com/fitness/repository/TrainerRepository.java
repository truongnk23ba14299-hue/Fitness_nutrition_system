package com.fitness.repository;

import com.fitness.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    /**
     * Lấy danh sách các huấn luyện viên đang hoạt động
     */
    List<Trainer> findByIsActiveTrue();

    /**
     * Lọc danh sách huấn luyện viên theo chuyên môn (specialty)
     */
    List<Trainer> findBySpecialtyContainingIgnoreCase(String specialty);

    /**
     * Lọc danh sách huấn luyện viên đang hoạt động theo chuyên môn
     */
    List<Trainer> findBySpecialtyContainingIgnoreCaseAndIsActiveTrue(String specialty);

    //Tìm kiếm theo từ khóa (Tên hoặc Chuyên môn) và lọc trạng thái
    @Query("SELECT t FROM Trainer t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.specialty) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive) " +
           "ORDER BY t.id DESC")
    List<Trainer> searchTrainersForAdmin(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive
    );
}
