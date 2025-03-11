package com.broker.stock.service.business;

import com.broker.stock.aspect.AuthorizeCustomer;
import com.broker.stock.aspect.CustomerId;
import com.broker.stock.entity.Asset;
import com.broker.stock.model.AssetResponse;
import com.broker.stock.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public Optional<Asset> getAsset(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
    }

    public void save(Asset asset) {
        assetRepository.save(asset);
    }

    @AuthorizeCustomer
    public List<AssetResponse> listAssets(@CustomerId Long customerId, String assetName, BigDecimal minUsableSize) {
        List<Asset> customerAssets = assetRepository.findByCustomerId(customerId);

        // Apply optional filters if provided
        return customerAssets.stream()
            .filter(asset -> assetName == null || asset.getAssetName().equalsIgnoreCase(assetName)) // Filter by asset name
            .filter(asset -> minUsableSize == null || asset.getUsableSize().compareTo(minUsableSize) >= 0)
            .map(asset -> new AssetResponse(asset.getCustomerId(),
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize().setScale(4)))// Filter by usable size
            .toList();
    }
}
