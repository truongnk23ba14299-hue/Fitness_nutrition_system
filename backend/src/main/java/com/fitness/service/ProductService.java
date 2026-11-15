package com.fitness.service;
import com.fitness.dto.ProductResponse;
import com.fitness.dto.ProductDetailResponse;
import com.fitness.model.Product;
import com.fitness.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductResponse> getAllProducts(){
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductResponse::fromEntity).toList();
    }

    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy sản phẩm với id: " + id));
        return ProductDetailResponse.fromEntity(product);
    }

}



