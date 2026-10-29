package main.java.com.fitness.controller;
import main.java.com.fitness.dto.EquiqmentDTO;
import main.java.com.fitness.dto.EquiqmentDetailDTO;
import main.java.com.fitness.Service.EquiqmentService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
public class EquipmentController {
    private final EquipmentService equipmentService;

    public  EquipmentController(EquipmentService equipmentService){
        this.equipmentService = equipmentService;
    }


    @GetMapping("/equipment")
    public ResponseEntity<List<EquipmentDTO>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipments);
    }

    
    @GetMapping("/equipments/{id}")
    public ResponseEntity<EquipmentDetailDTO> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
   
}
