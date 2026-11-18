package com.fitness.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_status", columnList = "status"),
        @Index(name = "idx_products_category", columnList = "category")
})
@Check(constraints = "stock_quantity >= 0 AND price > 0")
@Getter
@Setter

public class Product {

    public enum ProductStatus{
        ACTIVE,
        OUT_OF_STOCK,
        INACTIVE,
        DISCONTINUED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "weight_kg", precision = 8, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "image_url",columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name= "status", nullable = false)
    private ProductStatus productStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void decreaseStock(int qty){
        if (qty <= 0){
            throw new IllegalArgumentException("qty must be greater than 0");
        }
        if(this.stockQuantity == null || this.stockQuantity < qty){
            throw new IllegalArgumentException("Not enough stock for product " + this.id);
        }
        this.stockQuantity -= qty;
        if(this.stockQuantity == 0){
            this.productStatus = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void increaseStock(int qty){
        if (qty <= 0){
            throw new IllegalArgumentException("qty must be greater than 0");
        }
        this.stockQuantity += qty;
        if(this.productStatus == ProductStatus.OUT_OF_STOCK && this.stockQuantity > 0 ){
            this.productStatus = ProductStatus.ACTIVE;
        }
    }

}
