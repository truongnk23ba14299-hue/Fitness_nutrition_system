package com.fitness.controller;
import com.fitness.dto.EquipmentResponse;
import com.fitness.service.AddEquipmentService;
import com.fitness.dto.AddEquipmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/equipments")
public class AddEquipmentController {
    public final AddEquipmentService equipmentService;

    public AddEquipmentController(AddEquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    public ResponseEntity<EquipmentResponse> addNewEquipment(@Valid @RequestBody AddEquipmentRequest req ){
        EquipmentResponse new_equipment = equipmentService.addNewEquipment(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new_equipment);
    }

}
