package com.fitness.repository;

import com.fitness.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // Tìm kiếm / lọc thiết bị theo nhóm cơ tác động (ví dụ: tìm máy tập "Ngực", "Cardio"...)
    List<Equipment> findByTargetMuscleContainingIgnoreCase(String targetMuscle);
}