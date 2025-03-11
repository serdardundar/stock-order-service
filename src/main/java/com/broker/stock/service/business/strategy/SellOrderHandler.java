package com.broker.stock.service.business.strategy;

import com.broker.stock.entity.Asset;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.service.business.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellOrderHandler implements OrderHandler {

    private final AssetService assetService;
    @Override
    public void handleOrder(OrderRequest orderRequest) {
        // Retrieve and validate the asset to be sold
        Asset asset = assetService.getAsset(orderRequest.customerId(), orderRequest.assetName())
            .orElseThrow(() -> new IllegalArgumentException("Insufficient asset balance."));

        // Validate sufficient usable size for the sell order
        if (asset.getUsableSize().compareTo(orderRequest.size()) < 0) {
            throw new IllegalArgumentException("Insufficient asset balance.");
        }

        // Update asset for the sell order
        asset.setUsableSize(asset.getUsableSize().subtract(orderRequest.size()));
        assetService.save(asset);
    }
}
