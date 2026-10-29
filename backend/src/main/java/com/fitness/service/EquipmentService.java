package main.java.com.fitness.service;
import com.example.demo.dto.EquipmentDTO;
import com.example.demo.dto.EquipmentDetailDTO;
import com.example.demo.model.Equipments;
import com.example.demo.repository.EquipmentRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository sellingRepository) {
        this.equipmentRepository = sellingRepository;
    }

    public List<EquipmentDTO> getAllEquipments(){
        List<Equipments> equipments = equipmentRepository.findAll();
        if (equipments.isEmpty()) {
            throw new IllegalStateException("Không thể lấy danh sách");
        }

        return equipmentRepository.findAll().stream()
                .map(e -> new EquipmentDTO(
                        e.getId(),
                        e.getName(),
                        e.getDescription(),
                        e.getBrand(),
                        e.getPrice(),
                        e.getStockQty(),
                        e.getWeightKg()

                )).toList();

    }

    public EquipmentDetailDTO getEquipmentById(Long id) {
        Equipments equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy dụng cụ với id: " + id));
        return toDTO(equipment);
    }

    private EquipmentDetailDTO toDTO(Equipments equipment) {
        if (equipment == null) {
            throw new IllegalStateException("Dụng cụ ko đc null");
        }

        if (equipment.getId() == null
            || equipment.getName() == null
            || equipment.getName().isBlank()
            || equipment.getCategory() == null
            || equipment.getCategory().isBlank()
            || equipment.getPrice() == null
            || equipment.getPrice().signum() < 0
            || equipment.getStockQty() == null
            || equipment.getStockQty() < 0 ){

            throw new IllegalStateException("Dụng cụ với id ko hợp lệ: " +equipment.getId());
            }

        return new EquipmentDetailDTO(
                equipment.getId(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getBrand(),
                equipment.getPrice(),
                equipment.getStockQty(),
                equipment.getImageUrl(),
                equipment.getWeightKg(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()

          );

        }
    }



