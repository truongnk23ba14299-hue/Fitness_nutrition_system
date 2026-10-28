package com.fitness.repository;

import com.fitness.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
