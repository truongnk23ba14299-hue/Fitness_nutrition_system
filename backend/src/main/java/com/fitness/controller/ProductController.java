package com.fitness.controller;

import com.fitness.dto.AddProductRequest;
import com.fitness.dto.ProductResponse;
import com.fitness.dto.ProductDetailResponse;
import com.fitness.service.AddProductService;
import com.fitness.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final AddProductService addProductService;

    public ProductController(ProductService productService, AddProductService addProductService) {
        this.productService = productService;
        this.addProductService = addProductService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> addNewProduct(@Valid @RequestBody AddProductRequest req ){
        ProductResponse new_product = addProductService.addNewProduct(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new_product);
    }
}
