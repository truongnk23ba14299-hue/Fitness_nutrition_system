package com.fitness.model;
import jakarta.persistence.*;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name= "products")
@Getter
@Setter


public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name",  nullable= false, length=150)
    private String name;

    @Column(name= "description")
    private String description;

    @Column(name="category", nullable= false, length=100 )
    private String category;

    @Column(name= "brand", length=100)
    private String brand;

    @Column(name= "price", nullable=false, precision=12, scale=2)
    private BigDecimal price;

    @Column(name ="stock_quantity", nullable= false )
    private Integer stockQty;

    @Column(name= "weight_kg", precision= 8, scale=2)
    private BigDecimal weightKg;

    @Column(name= "image_url")
    private String imageUrl;

    @Column(name= "created_at", insertable=false, updatable=false )
    private LocalDateTime createdAt;

    @Column(name= "updated_at", insertable=false, updatable=false  )
    private LocalDateTime updatedAt;

    public Products(){ }


}
