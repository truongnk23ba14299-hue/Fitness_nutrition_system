package com.fitness.service;

import com.fitness.dto.AdminPackageResponse;
import com.fitness.dto.PackageDeleteResponse;
import com.fitness.dto.PackageDetailResponse;
import com.fitness.dto.PackageStatisticsResponse;
import com.fitness.dto.UpdatePackageRequest;
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
    public AdminPackageResponse updatePackage(Long id, UpdatePackageRequest request) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời hạn gói tập phải lớn hơn 0 tháng");
        }

        pkg.setTitle(request.getTitle().trim());
        pkg.setPrice(request.getPrice());
        pkg.setDurationMonths(request.getDurationMonths());
        if (request.getDescription() != null) pkg.setDescription(request.getDescription());
        if (request.getImageUrl() != null) pkg.setImageUrl(request.getImageUrl());
        if (request.getIsActive() != null) pkg.setIsActive(request.getIsActive());
        if (request.getBenefits() != null) pkg.setBenefits(request.getBenefits());
        if (request.getScheduleDetails() != null) pkg.setScheduleDetails(request.getScheduleDetails());
        if (request.getTrainerInfo() != null) pkg.setTrainerInfo(request.getTrainerInfo());

        return AdminPackageResponse.fromEntity(repository.save(pkg));
    }

    @Transactional
    public PackageDeleteResponse deletePackage(Long id) {
        WorkoutPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));

        pkg.setIsActive(false);
        repository.save(pkg);

        return new PackageDeleteResponse("Đã ẩn/xóa gói tập thành công", id);
    }

    @Transactional(readOnly = true)
    public PackageStatisticsResponse getPackageStatistics(Long id) {
        WorkoutPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));

        long activeStudents = enrollmentRepository.countByWorkoutPackageIdAndStatus(id, EnrollmentStatus.ACTIVE);
        long totalEnrollments = enrollmentRepository.countByWorkoutPackageId(id);

        return new PackageStatisticsResponse(
                pkg.getId(),
                pkg.getTitle(),
                pkg.getIsActive(),
                activeStudents,
                totalEnrollments
        );
    }
}