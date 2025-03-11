package com.broker.stock.repository;

import com.broker.stock.constant.OrderStatus;
import com.broker.stock.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByStatusOrderById(OrderStatus status);
}
