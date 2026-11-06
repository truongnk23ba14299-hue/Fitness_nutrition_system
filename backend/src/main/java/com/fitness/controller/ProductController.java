package com.fitness.controller;

import com.fitness.dto.AddProductRequest;
import com.fitness.dto.ProductResponse;
import com.fitness.dto.ProductDetailResponse;
import com.fitness.service.AddProductService;
import com.fitness.service.ProductService;
import com.fitness.service.UpdateProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;
    private final AddProductService addProductService;
    private final UpdateProductService updateProductService;

    public ProductController(ProductService productService, AddProductService addProductService, UpdateProductService updateProductService) {
        this.productService = productService;
        this.addProductService = addProductService;
        this.updateProductService = updateProductService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/admin/product")
    public ResponseEntity<ProductResponse> addNewProduct(@Valid @RequestBody AddProductRequest req ){
        ProductResponse new_product = addProductService.addNewProduct(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new_product);
    }

    @PutMapping("/admin/product/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody AddProductRequest req ){
        ProductResponse response = updateProductService.updateProduct(id, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        updateProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
