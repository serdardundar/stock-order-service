package com.broker.stock.service.business.strategy;

import com.broker.stock.model.OrderRequest;

public interface OrderHandler {
    void handleOrder(OrderRequest orderRequest);
}
