package com.fitness.controller;
import com.fitness.dto.ProductRequest;
import com.fitness.dto.ProductResponse;
import com.fitness.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<ProductResponse> addNewProducts(@Valid @RequestBody ProductRequest req) {
        ProductResponse newProduct = adminProductService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProducts(@PathVariable Long id, @Valid @RequestBody ProductRequest req){
        ProductResponse response = adminProductService.update(id, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        adminProductService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
