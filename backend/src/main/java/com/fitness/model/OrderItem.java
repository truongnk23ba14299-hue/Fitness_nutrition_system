package com.fitness.model;
<<<<<<< HEAD

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ID sản phẩm trong Shop. Dùng Long để không phụ thuộc cấu trúc Product của thành viên khác.
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Lưu tên sản phẩm tại thời điểm đặt hàng (snapshot, tránh sai dữ liệu khi sản phẩm đổi tên/xóa).
    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;
=======
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
>>>>>>> 0a0714c (Update Product & Add Order API)
}
