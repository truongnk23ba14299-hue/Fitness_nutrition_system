package com.fitness.service;
import com.fitness.dto.*;
import com.fitness.repository.EquipmentRepository;
import com.fitness.model.Equipments;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
public class AddEquipmentService {
    private final EquipmentRepository equipmentRepository ;


    public AddEquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public EquipmentResponse addNewEquipment(AddEquipmentRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("Equipment name is required");
        }

        if (req.category() == null || req.category().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }

        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");

        }

        if (req.stockQuantity() == null || req.stockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must be >= 0");

        }

        if (req.weightKg() != null && req.weightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight must be > 0");

        }

        Equipments equipment = createEquipment(req);
        Equipments save_newEquipment = equipmentRepository.save(equipment);

        return new EquipmentResponse(
                save_newEquipment.getId(),
                save_newEquipment.getName(),
                save_newEquipment.getDescription(),
                save_newEquipment.getBrand(),
                save_newEquipment.getPrice(),
                save_newEquipment.getStockQty(),
                save_newEquipment.getWeightKg()

        );
    }

    private static @NonNull Equipments createEquipment(AddEquipmentRequest req) {
        Equipments equipment = new Equipments();
        equipment.setName(req.name().trim());
        equipment.setDescription(req.description());
        equipment.setCategory(req.category().trim());
        equipment.setBrand(req.brand());
        equipment.setPrice(req.price());
        equipment.setStockQty(req.stockQuantity());
        equipment.setImageUrl(req.imageUrl());
        equipment.setWeightKg(req.weightKg());
        equipment.setCreatedAt(req.created_at());
        equipment.setUpdatedAt(req.updated_at());
        return equipment;
    }
}
