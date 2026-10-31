package com.example.demo.controller;

import com.example.demo.dto.PackageResponse;
import com.example.demo.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/packages") // Chuẩn RESTful theo yêu cầu nhóm: không có /v1/
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping
    public ResponseEntity<List<PackageResponse>> getPackages() {
        List<PackageResponse> responseList = packageService.getAllActivePackages();
        return ResponseEntity.ok(responseList);
    }
}
