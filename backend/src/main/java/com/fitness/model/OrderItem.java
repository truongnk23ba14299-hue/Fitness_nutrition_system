package com.fitness.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_items_order_product",
                        columnNames = {"order_id", "product_id"})
        },
        indexes = {
                @Index(name = "idx_order_items_product_id", columnList = "product_id")
        })
@Check(constraints = "quantity > 0")
@Getter
@Setter
@NoArgsConstructor

public class OrderItem {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "order_id", nullable= false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable= false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name= "unit_price", nullable = false, precision= 12, scale= 2)
    private BigDecimal unitPrice;

    @Column(name= "sub_total", nullable= false, precision= 12, scale= 2)
    private BigDecimal subTotal;
}
