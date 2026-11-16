package com.fitness.controller;
import com.fitness.dto.AdminProductResponse;
import com.fitness.dto.ProductRequest;
import com.fitness.dto.ProductResponse;
import com.fitness.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

// Minh Quang: hỗ trợ CẢ HAI đường dẫn số nhiều (/api/admin/products) và số ít (/api/admin/product)
// theo đúng đề bài: PUT /api/admin/product/{id} (Sửa), DELETE /api/admin/product/{id} (Xóa)
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final AdminProductService adminProductService;

    @GetMapping("/api/admin/products")
    public ResponseEntity<List<AdminProductResponse>> getAdminProducts() {
        return ResponseEntity.ok(adminProductService.getAllProducts());
    }

    @GetMapping({"/api/admin/products/{id}", "/api/admin/product/{id}"})
    public ResponseEntity<AdminProductResponse> getAdminProductById(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.getProductById(id));
    }

    @PostMapping("/api/admin/products")
    public ResponseEntity<ProductResponse> addNewProducts(@Valid @RequestBody ProductRequest req) {
        ProductResponse newProduct = adminProductService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @PutMapping({"/api/admin/products/{id}", "/api/admin/product/{id}"})
    public ResponseEntity<AdminProductResponse> updateProducts(@PathVariable Long id, @Valid @RequestBody ProductRequest req){
        AdminProductResponse response = adminProductService.update(id, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping({"/api/admin/products/{id}", "/api/admin/product/{id}"})
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        adminProductService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==== Minh Quang: PUT /api/admin/products/{id}/stock — Nhập thêm tồn kho khi có đợt hàng mới ====
    @PutMapping("/api/admin/products/{id}/stock")
    public ResponseEntity<AdminProductResponse> updateProductStock(
            @PathVariable Long id,
            @Valid @RequestBody com.fitness.dto.UpdateStockRequest req) {
        return ResponseEntity.ok(adminProductService.addStock(id, req.additionalStock()));
    }
}
