package com.fitness.service;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.model.EnrollmentStatus;
import com.fitness.model.WorkoutPackage;
import com.fitness.repository.PackageEnrollmentRepository;
import com.fitness.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PackageDetailService {

    private final PackageRepository repository;
    private final PackageEnrollmentRepository enrollmentRepository;

    public PackageDetailResponse getPackageDetailById(Long id) {
        return repository.findById(id)
                .map(PackageDetailResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));
    }

    @Transactional
    public WorkoutPackage updatePackage(Long id, WorkoutPackage request) {
        WorkoutPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên gói tập không được để trống");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá gói tập phải lớn hơn 0");
        }
        if (request.getDurationMonths() == null || request.getDurationMonths() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời hạn gói tập phải lớn hơn 0");
        }

        pkg.setTitle(request.getTitle());
        pkg.setPrice(request.getPrice());
        pkg.setDurationMonths(request.getDurationMonths());
        pkg.setDescription(request.getDescription());
        pkg.setImageUrl(request.getImageUrl());
        if (request.getIsActive() != null) {
            pkg.setIsActive(request.getIsActive());
        }
        pkg.setBenefits(request.getBenefits());
        pkg.setScheduleDetails(request.getScheduleDetails());
        pkg.setTrainerInfo(request.getTrainerInfo());

        return repository.save(pkg);
    }

    @Transactional
    public Map<String, Object> deletePackage(Long id) {
        WorkoutPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));

        pkg.setIsActive(false);
        repository.save(pkg);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Đã ẩn/xóa gói tập thành công");
        result.put("packageId", id);
        return result;
    }

    public Map<String, Object> getPackageStatistics(Long id) {
        WorkoutPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));

        long activeStudents = enrollmentRepository.countByWorkoutPackageIdAndStatus(id, EnrollmentStatus.ACTIVE);
        long totalEnrollments = enrollmentRepository.countByWorkoutPackageId(id);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("packageId", pkg.getId());
        stats.put("packageTitle", pkg.getTitle());
        stats.put("isActive", pkg.getIsActive());
        stats.put("activeStudents", activeStudents);
        stats.put("totalEnrollments", totalEnrollments);
        return stats;
    }
}