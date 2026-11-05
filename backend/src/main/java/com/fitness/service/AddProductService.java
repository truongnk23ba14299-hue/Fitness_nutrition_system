package com.fitness.service;
import com.fitness.dto.*;
import com.fitness.repository.ProductRepository;
import com.fitness.model.Products;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AddProductService {
    private final ProductRepository productRepository ;

    public AddProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse addNewProduct(AddProductRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (req.category() == null || req.category().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }

        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");

        }

        if (req.stockQuantity() == null || req.stockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must be >= 0");

        }

        if (req.weightKg() != null && req.weightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight must be > 0");

        }

        Products product = createProduct(req);
        Products new_product = productRepository.save(product);

        return new ProductResponse(
                new_product.getId(),
                new_product.getName(),
                new_product.getDescription(),
                new_product.getBrand(),
                new_product.getPrice(),
                new_product.getStockQty(),
                new_product.getWeightKg()
        );
    }

    private static @NonNull Products createProduct(AddProductRequest req) {
        Products product = new Products();
        product.setName(req.name().trim());
        product.setDescription(req.description());
        product.setCategory(req.category().trim());
        product.setBrand(req.brand());
        product.setPrice(req.price());
        product.setStockQty(req.stockQuantity());
        product.setImageUrl(req.imageUrl());
        product.setWeightKg(req.weightKg());
        product.setCreatedAt(req.created_at());
        product.setUpdatedAt(req.updated_at());
        return product;
    }
}
