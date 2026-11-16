package com.fitness.service;
import com.fitness.dto.AdminProductResponse;
import com.fitness.dto.ProductResponse;
import com.fitness.dto.ProductRequest;
import com.fitness.repository.ProductRepository;
import com.fitness.model.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitness.Exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<AdminProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(AdminProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProductResponse getProductById(Long id) {
        Product product =  productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " +id));
        return AdminProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse create(@Valid ProductRequest req) {
        Product product = new Product();
        applyRequest(product, req);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product product = findOrThrow(id);
        applyRequest(product, req);
        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public void delete(Long id){
        productRepository.deleteById(findOrThrow(id).getId());
    }

    // ==== Minh Quang: Nhập thêm số lượng tồn kho (stockQuantity) khi có đợt hàng mới ====
    @Transactional
    public AdminProductResponse addStock(Long id, Integer additionalStock) {
        Product product = findOrThrow(id);
        if (additionalStock == null || additionalStock <= 0) {
            throw new IllegalArgumentException("Số lượng nhập thêm phải lớn hơn 0");
        }
        product.setStockQuantity(product.getStockQuantity() + additionalStock);
        // Hết hàng trước đó (OUT_OF_STOCK) -> tự bật lại ACTIVE sau khi nhập hàng
        if (product.getProductStatus() == Product.ProductStatus.OUT_OF_STOCK
                && product.getStockQuantity() > 0) {
            product.setProductStatus(Product.ProductStatus.ACTIVE);
        }
        product.setUpdatedAt(LocalDateTime.now());
        return AdminProductResponse.fromEntity(product);
    }

    private Product findOrThrow(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ko tìm thấy sản phẩm vs id: " +id));
    }

    private void applyRequest(Product product, @Valid ProductRequest req) {
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
