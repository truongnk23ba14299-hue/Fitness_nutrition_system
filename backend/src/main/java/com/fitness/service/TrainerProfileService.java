package com.fitness.service;

import com.fitness.dto.TrainerProfileResponse;
import com.fitness.dto.TrainerProfileUpdateRequest;
import com.fitness.model.Trainer;
import com.fitness.model.User;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TrainerProfileService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    // 1. GET /api/trainer/profile: Lấy thông tin Bio cá nhân khi đăng nhập
    @Transactional(readOnly = true)
    public TrainerProfileResponse getMyProfile(String email) {
        Trainer trainer = currentTrainer(email);
        return TrainerProfileResponse.fromEntity(trainer);
    }

    // 2. PUT /api/trainer/profile: HLV tự cập nhật Bio và kinh nghiệm cá nhân
    @Transactional
    public TrainerProfileResponse updateMyProfile(String email, TrainerProfileUpdateRequest request) {
        Trainer trainer = currentTrainer(email);

        if (request.bio() != null) {
            String bio = request.bio().trim();
            trainer.setBio(bio.isEmpty() ? null : bio);
        }
        if (request.experienceYears() != null) {
            trainer.setExperienceYears(request.experienceYears());
        }

        Trainer updated = trainerRepository.save(trainer);
        return TrainerProfileResponse.fromEntity(updated);
    }

    // Helper: Xác định hồ sơ HLV gắn với tài khoản đang đăng nhập
    private Trainer currentTrainer(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản người dùng!"));
        return trainerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Tài khoản chưa được liên kết với hồ sơ huấn luyện viên!"));
    }
}
