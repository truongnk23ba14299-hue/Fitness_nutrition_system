package com.fitness.controller;

import com.fitness.dto.EquipmentResponse;
import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.service.EquipmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService){
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipments() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDetailResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
}
