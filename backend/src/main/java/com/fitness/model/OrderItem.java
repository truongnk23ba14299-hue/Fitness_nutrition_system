package com.fitness.model;

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
}
