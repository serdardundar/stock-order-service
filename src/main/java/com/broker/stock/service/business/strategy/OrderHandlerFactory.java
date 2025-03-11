package com.broker.stock.service.business.strategy;

import com.broker.stock.constant.OrderSide;
import com.broker.stock.service.business.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderHandlerFactory {

    private final AssetService assetService;

    public OrderHandler getHandler(OrderSide orderSide) {
        return switch (orderSide) {
            case BUY -> new BuyOrderHandler(assetService);
            case SELL -> new SellOrderHandler(assetService);
        };
    }
}

