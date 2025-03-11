package com.broker.stock.entity;

import com.broker.stock.constant.OrderSide;
import com.broker.stock.constant.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING) // Store enum as a string in the database
    @Column(name = "order_side", nullable = false)
    private OrderSide orderSide;

    @Column(name = "size", nullable = false, precision = 20, scale = 8)
    private BigDecimal size;

    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Enumerated(EnumType.STRING) // Store enum as a string in the database
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;
}
