package com.fitness.service;
import com.fitness.dto.AddProductRequest;
import com.fitness.dto.ProductResponse;
import com.fitness.model.Products;
import com.fitness.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UpdateProductService {
    private final ProductRepository productRepository;


    public UpdateProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse updateProduct(Long id, AddProductRequest req){
        if (id == null) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống");
        }
        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        if (req.stockQuantity() == null || req.stockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must be >= 0");
        }

        Products product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy sản phẩm với id: " + id));

        product.setPrice(req.price());
        product.setStockQty(req.stockQuantity());
        product.setUpdatedAt(LocalDateTime.now());

        Products updated_product = productRepository.save(product);
        return ProductResponse.fromEntity(updated_product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống");
        }
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Không tìm thấy sản phẩm với id: " + id);
        }
        productRepository.deleteById(id);
    }
}



