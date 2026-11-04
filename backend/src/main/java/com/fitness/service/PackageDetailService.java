package com.fitness.service;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.repository.PackageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PackageDetailService {

    private final PackageRepository repository;

    public PackageDetailService(PackageRepository repository) {
        this.repository = repository;
    }

    public PackageDetailResponse getPackageDetailById(Long id) {
        return repository.findById(id)
                .map(PackageDetailResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy gói tập với ID: " + id
                ));
    }
}
