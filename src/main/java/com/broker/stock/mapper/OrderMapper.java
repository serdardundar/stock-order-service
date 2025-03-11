package com.broker.stock.mapper;

import com.broker.stock.constant.OrderStatus;
import com.broker.stock.entity.Order;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.model.OrderResponse;

import java.time.LocalDateTime;

public class OrderMapper {

    private OrderMapper() {
        // Private constructor to prevent instantiation (utility class)
    }

    /**
     * Converts an OrderRequest DTO to an Order entity.
     *
     * @param orderRequest the incoming OrderRequest
     * @return a populated Order entity
     */
    public static Order toEntity(OrderRequest orderRequest) {
        var order = new Order();
        order.setCustomerId(orderRequest.customerId());
        order.setAssetName(orderRequest.assetName());
        order.setOrderSide(orderRequest.orderSide());
        order.setSize(orderRequest.size());
        order.setPrice(orderRequest.price());
        order.setStatus(OrderStatus.PENDING); // Set default status
        order.setCreateDate(LocalDateTime.now()); // Set current timestamp
        return order;
    }

    /**
     * Converts an Order entity to an OrderResponse DTO.
     *
     * @param order the Order entity
     * @return a populated OrderResponse DTO
     */
    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .customerId(order.getCustomerId())
            .assetName(order.getAssetName())
            .orderSide(order.getOrderSide())
            .status(order.getStatus())
            .createDate(order.getCreateDate())
            .build();
    }
}
