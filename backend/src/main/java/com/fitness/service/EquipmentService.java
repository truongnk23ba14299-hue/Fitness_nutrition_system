package com.fitness.service;

import com.fitness.dto.EquipmentDeleteResponse;
import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.dto.EquipmentRequest;
import com.fitness.dto.EquipmentResponse;
import com.fitness.model.Equipment;
import com.fitness.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    // 1. Public: Chỉ trả về thông tin tóm tắt (ẩn workoutGuide và videoUrl)
    @Transactional(readOnly = true)
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

    // 2. Authenticated: Trả về chi tiết kèm hướng dẫn & link video hướng dẫn sử dụng máy
    @Transactional(readOnly = true)
    public EquipmentDetailResponse getEquipmentById(Long id) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thiết bị với ID: " + id
                ));

        return toDetailResponse(eq);
    }

    // 3. Admin: Thêm máy tập mới
    @Transactional
    public EquipmentDetailResponse addEquipment(EquipmentRequest request) {
        Equipment equipment = Equipment.builder()
                .name(request.name().trim())
                .brand(request.brand())
                .imageUrl(request.imageUrl())
                .videoUrl(request.videoUrl())
                .workoutGuide(request.workoutGuide().trim())
                .targetMuscle(request.targetMuscle().trim())
                .build();

        return toDetailResponse(equipmentRepository.save(equipment));
    }

    // 4. Admin: Cập nhật thông tin & video hướng dẫn máy tập
    @Transactional
    public EquipmentDetailResponse updateEquipment(Long id, EquipmentRequest request) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thiết bị với ID: " + id
                ));

        eq.setName(request.name().trim());
        if (request.brand() != null) eq.setBrand(request.brand());
        if (request.imageUrl() != null) eq.setImageUrl(request.imageUrl());
        if (request.videoUrl() != null) eq.setVideoUrl(request.videoUrl());
        eq.setWorkoutGuide(request.workoutGuide().trim());
        eq.setTargetMuscle(request.targetMuscle().trim());

        return toDetailResponse(equipmentRepository.save(eq));
    }

    // 5. Admin: Xóa thiết bị
    @Transactional
    public EquipmentDeleteResponse deleteEquipment(Long id) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thiết bị với ID: " + id
                ));

        equipmentRepository.delete(eq);
        return new EquipmentDeleteResponse("Đã xóa thiết bị thành công", id);
    }

    private EquipmentDetailResponse toDetailResponse(Equipment eq) {
        return new EquipmentDetailResponse(
                eq.getId(),
                eq.getName(),
                eq.getBrand(),
                eq.getImageUrl(),
                eq.getVideoUrl(),
                eq.getWorkoutGuide(),
                eq.getTargetMuscle(),
                eq.getCreatedAt()
        );
    }
}