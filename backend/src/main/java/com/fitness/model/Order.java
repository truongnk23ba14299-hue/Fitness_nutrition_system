package com.fitness.model;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "order_status")
})
@Getter
@Setter
@NoArgsConstructor

public class Order {

    // Minh Quang: thêm COMPLETED theo đề bài Admin Orders
    // (PENDING -> CONFIRMED -> SHIPPING -> COMPLETED / CANCELLED); DELIVERED giữ nguyên của bản gốc
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        SHIPPING,
        DELIVERED,
        COMPLETED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="user_id", nullable=false)
    private User user;

    @Column(name= "receiver_name", nullable= false, length= 100)
    private String receiverName;

    @Column(name= "phone", nullable= false, length= 20)
    private String phone;

    @Column(name = "shipping_address", nullable= false, length= 500)
    private String shippingAddress;

    @Column(name="total_amount", nullable = false, precision= 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable= false, length= 30)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @CreationTimestamp
    @Column(name= "created_at",nullable= false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name= "updated_at")
    private LocalDateTime updatedAt;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy= "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem>  items = new ArrayList<>();

    public void addItem(OrderItem item){
        item.setOrder(this);
        this.items.add(item);
    }

    public void confirm(){
        requiredStatus(OrderStatus.PENDING);
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void ship(){
        requiredStatus(OrderStatus.CONFIRMED);
        this.orderStatus = OrderStatus.SHIPPING;
    }

    public void deliver(){
        requiredStatus(OrderStatus.SHIPPING);
        this.orderStatus = OrderStatus.DELIVERED;
    }

    public void cancel(){
        if (orderStatus != OrderStatus.PENDING && orderStatus != OrderStatus.CONFIRMED){
            throw new  IllegalStateException("Cannot cancel order in status " + orderStatus);
        }
        this.orderStatus = OrderStatus.CANCELLED;
        items.forEach(i -> i.getProduct().increaseStock(i.getQuantity()));
    }

    public void requiredStatus(OrderStatus expected){
        if(this.orderStatus != expected){
            throw new  IllegalStateException("Invalid status transition from " + this.orderStatus + " to " + expected);
        }
    }

    // ==== Minh Quang: Admin duyệt đơn hoàn tất (SHIPPING/DELIVERED -> COMPLETED) theo đề bài ====
    public void complete(){
        if (orderStatus != OrderStatus.SHIPPING && orderStatus != OrderStatus.DELIVERED){
            throw new  IllegalStateException("Invalid status transition from " + this.orderStatus + " to " + OrderStatus.COMPLETED);
        }
        this.orderStatus = OrderStatus.COMPLETED;
    }

}
