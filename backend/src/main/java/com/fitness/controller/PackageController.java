@'
package com.fitness.controller;

import com.fitness.dto.PackageDetailResponse;
import com.fitness.dto.PackageResponse;
import com.fitness.service.PackageDetailService;
import com.fitness.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "*")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageDetailService packageDetailService;

    @GetMapping
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageDetailResponse> getPackageDetail(@PathVariable Long id) {
        return ResponseEntity.ok(packageDetailService.getPackageDetailById(id));
    }
}
'@ | Set-Content -Path "src\main\java\com\fitness\controller\PackageController.java" -Encoding UTF8