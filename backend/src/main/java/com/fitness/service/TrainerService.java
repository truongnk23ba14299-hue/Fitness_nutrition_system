package com.fitness.service;

import com.fitness.dto.TrainerResponse;
import com.fitness.model.Trainer;
import com.fitness.repository.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    /**
     * Lấy danh sách huấn luyện viên thể hình (PT).
     * Hỗ trợ lọc theo chuyên môn (specialty).
     *
     * @param specialty Chuyên môn cần lọc (ví dụ: Gym, Thể hình, Yoga, Cardio, ...)
     * @return Danh sách DTO TrainerResponse
     */
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainers(String specialty) {
        List<Trainer> trainers;

        if (specialty != null && !specialty.trim().isEmpty()) {
            trainers = trainerRepository.findBySpecialtyContainingIgnoreCaseAndIsActiveTrue(specialty.trim());
            // Fallback nếu không có bản ghi active khớp, thử tìm trong toàn bộ
            if (trainers.isEmpty()) {
                trainers = trainerRepository.findBySpecialtyContainingIgnoreCase(specialty.trim());
            }
        } else {
            trainers = trainerRepository.findByIsActiveTrue();
            // Fallback nếu trường is_active chưa được đánh dấu hoặc toàn bộ là null
            if (trainers.isEmpty()) {
                trainers = trainerRepository.findAll();
            }
        }

        return trainers.stream()
                .map(TrainerResponse::fromEntity)
                .toList();
    }

    /**
     * Lấy tất cả danh sách PT không qua lọc chuyên môn
     *
     * @return Danh sách DTO TrainerResponse
     */
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainers() {
        return getAllTrainers(null);
    }

    /**
     * Lấy thông tin chi tiết một PT theo ID
     *
     * @param id Mã định danh của Huấn luyện viên
     * @return DTO TrainerResponse hoặc null nếu không tìm thấy
     */
    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(Long id) {
        return trainerRepository.findById(id)
                .map(TrainerResponse::fromEntity)
                .orElse(null);
    }
}
