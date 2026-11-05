package com.fitness.service;
import com.fitness.dto.ProductResponse;
import com.fitness.dto.ProductDetailResponse;
import com.fitness.model.Products;
import com.fitness.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts(){
        List<Products> equipments = productRepository.findAll();

        return equipments.stream()
                .map(ProductResponse::fromEntity).toList();
    }

    public ProductDetailResponse getProductById(Long id) {
        Products product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy sản phẩm với id: " + id));

        return ProductDetailResponse.fromEntity(product);
    }
}



