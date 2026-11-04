package com.fitness.service;
import com.fitness.dto.EquipmentResponse;
import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.model.Equipments;
import com.fitness.repository.EquipmentRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<EquipmentResponse> getAllEquipments(){
        List<Equipments> equipments = equipmentRepository.findAll();
        if (equipments.isEmpty()) {
            throw new IllegalStateException("Không thể lấy danh sách");
        }

        return equipments.stream().map(EquipmentResponse::fromEntity).toList();


    }

    public EquipmentDetailResponse getEquipmentById(Long id) {
        Equipments equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy dụng cụ với id: " + id));

        return EquipmentDetailResponse.fromEntity(equipment);
    }
}



