package com.fitness.controller;

import com.fitness.dto.EquipmentDetailResponse;
import com.fitness.dto.EquipmentResponse;
import com.fitness.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping({"/equipment", "/equipments"})
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @GetMapping({"/equipment/{id}", "/equipments/{id}"})
    public ResponseEntity<EquipmentDetailResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
}
