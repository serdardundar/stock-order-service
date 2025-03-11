package com.broker.stock.service.business.strategy;

import static com.broker.stock.constant.AssetConstants.TRY;
import com.broker.stock.entity.Asset;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.service.business.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BuyOrderHandler implements OrderHandler {

    private final AssetService assetService;

    @Override
    public void handleOrder(OrderRequest orderRequest) {
        // Retrieve and validate TRY asset
        Asset tryAsset = assetService.getAsset(orderRequest.customerId(), TRY)
            .orElseThrow(() -> new IllegalArgumentException("Insufficient TRY balance."));

        BigDecimal requiredTRY = orderRequest.size().multiply(orderRequest.price());
        if (tryAsset.getUsableSize().compareTo(requiredTRY) < 0) {
            throw new IllegalStateException("Insufficient TRY balance.");
        }

        // Update TRY asset
        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(requiredTRY));
        assetService.save(tryAsset);

        // Check if the order's asset exists; if not, initialize it
        assetService.getAsset(orderRequest.customerId(), orderRequest.assetName())
            .ifPresentOrElse(
                asset -> {
                    // Do nothing; asset already exists
                },
                () -> {
                    // Initialize asset with zero values
                    Asset newAsset = new Asset();
                    newAsset.setCustomerId(orderRequest.customerId());
                    newAsset.setAssetName(orderRequest.assetName());
                    newAsset.setUsableSize(BigDecimal.ZERO);
                    newAsset.setSize(BigDecimal.ZERO);
                    assetService.save(newAsset);
                }
            );
    }
}
