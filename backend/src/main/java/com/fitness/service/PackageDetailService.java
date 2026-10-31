package com.fitness.service;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.repository.PackageDetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PackageDetailService {

    private final PackageDetailRepository repository;

    public PackageDetailService(PackageDetailRepository repository) {
        this.repository = repository;
    }

    public PackageDetailResponse getPackageDetailById(Long id) {
        return repository.findById(id)
                .map(PackageDetailResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Package not found with ID: " + id
                ));
    }
}
