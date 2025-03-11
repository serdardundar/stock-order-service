package com.broker.stock.model;


import com.broker.stock.aspect.CustomerId;
import com.broker.stock.constant.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


public record OrderRequest(
    @CustomerId
    @NotNull(message = "Customer ID cannot be null")
    Long customerId,

    @NotNull(message = "Asset name cannot be null")
    @Size(min = 1, message = "Asset name cannot be empty")
    String assetName,

    @NotNull(message = "Order side cannot be null")
    OrderSide orderSide,

    @NotNull(message = "Size cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Size must be greater than 0")
    BigDecimal size,

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price) {
}

