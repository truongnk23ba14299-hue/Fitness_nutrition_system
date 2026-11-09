package com.fitness.service;

import com.fitness.dto.EquipmentRequest;
import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.dto.EquipmentResponse;
import com.fitness.model.Equipment;
import com.fitness.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    // 1. Lấy danh sách tất cả máy tập (có hỗ trợ lọc theo nhóm cơ)
    public List<EquipmentResponse> getAllEquipments(String targetMuscle) {
        List<Equipment> list;
        if (targetMuscle != null && !targetMuscle.isBlank()) {
            list = equipmentRepository.findByTargetMuscleContainingIgnoreCase(targetMuscle.trim());
        } else {
            list = equipmentRepository.findAll();
        }

        return list.stream()
                .map(eq -> new EquipmentResponse(
                        eq.getId(),
                        eq.getName(),
                        eq.getBrand(),
                        eq.getImageUrl(),
                        eq.getTargetMuscle()
                ))
                .toList();
    }

    // 2. Xem chi tiết 1 máy tập kèm hướng dẫn bài tập theo ID
    public EquipmentDetailResponse getEquipmentById(Long id) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị với ID: " + id));

        return new EquipmentDetailResponse(
                eq.getId(),
                eq.getName(),
                eq.getBrand(),
                eq.getImageUrl(),
                eq.getWorkoutGuide(),
                eq.getTargetMuscle(),
                eq.getCreatedAt()
        );
    }

    // 3. Admin: Thêm máy tập mới vào hệ thống
    @Transactional
    public EquipmentDetailResponse addEquipment(EquipmentRequest request) {
        Equipment equipment = Equipment.builder()
                .name(request.name().trim())
                .brand(request.brand())
                .imageUrl(request.imageUrl())
                .workoutGuide(request.workoutGuide().trim())
                .targetMuscle(request.targetMuscle().trim())
                .build();

        Equipment saved = equipmentRepository.save(equipment);

        return new EquipmentDetailResponse(
                saved.getId(),
                saved.getName(),
                saved.getBrand(),
                saved.getImageUrl(),
                saved.getWorkoutGuide(),
                saved.getTargetMuscle(),
                saved.getCreatedAt()
        );
    }
}