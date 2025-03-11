package com.broker.stock.model;

import com.broker.stock.constant.OrderSide;
import com.broker.stock.constant.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderResponse(Long id,
                            Long customerId,
                            String assetName,
                            OrderSide orderSide,
                            OrderStatus status,
                            LocalDateTime createDate) {
}
