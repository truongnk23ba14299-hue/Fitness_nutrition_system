package com.fitness.service;
import com.fitness.dto.ProductResponse;
import com.fitness.dto.ProductRequest;
import com.fitness.repository.ProductRepository;
import com.fitness.model.Products;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitness.Exception.ResourceNotFoundException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductRepository productRepository ;

    @Transactional
    public ProductResponse create(@Valid ProductRequest req) {
        Products product = new Products();
        applyRequest(product, req);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Products product = findOrThrow(id);
        applyRequest(product, req);
        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public void delete(Long id){
        productRepository.deleteById(findOrThrow(id).getId());
    }

    private Products findOrThrow(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ko tìm thấy sản phẩm vs id: " +id));
    }

    private void applyRequest(Products product, @Valid ProductRequest req) {
        product.setName(req.name().trim());
        product.setDescription(req.description());
        product.setCategory(req.category().trim());
        product.setBrand(req.brand());
        product.setPrice(req.price());
        product.setStockQuantity(req.stockQuantity());
        product.setImageUrl(req.imageUrl());
        product.setWeightKg(req.weightKg());
    }
}
