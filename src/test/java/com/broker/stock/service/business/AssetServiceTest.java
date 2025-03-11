package com.broker.stock.service.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.broker.stock.entity.Asset;
import com.broker.stock.model.AssetResponse;
import com.broker.stock.repository.AssetRepository;
import com.broker.stock.service.business.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAsset_Successful() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";

        Asset mockAsset = new Asset();
        mockAsset.setCustomerId(customerId);
        mockAsset.setAssetName(assetName);
        mockAsset.setSize(BigDecimal.valueOf(100));
        mockAsset.setUsableSize(BigDecimal.valueOf(50));

        // Mock repository behavior
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(mockAsset));

        // When
        Optional<Asset> result = assetService.getAsset(customerId, assetName);

        // Then
        assertTrue(result.isPresent());
        assertEquals("GOLD", result.get().getAssetName());
        assertEquals(BigDecimal.valueOf(100), result.get().getSize());
        assertEquals(BigDecimal.valueOf(50), result.get().getUsableSize());
        verify(assetRepository, times(1)).findByCustomerIdAndAssetName(customerId, assetName);
    }

    @Test
    void testListAssets_NoFilters() {
        // Given
        Long customerId = 1L;

        Asset asset1 = new Asset();
        asset1.setCustomerId(customerId);
        asset1.setAssetName("GOLD");
        asset1.setSize(BigDecimal.valueOf(100));
        asset1.setUsableSize(BigDecimal.valueOf(70));

        Asset asset2 = new Asset();
        asset2.setCustomerId(customerId);
        asset2.setAssetName("SILVER");
        asset2.setSize(BigDecimal.valueOf(50));
        asset2.setUsableSize(BigDecimal.valueOf(20));

        List<Asset> mockAssets = List.of(asset1, asset2);

        // Mock repository behavior
        when(assetRepository.findByCustomerId(customerId)).thenReturn(mockAssets);

        // When
        List<AssetResponse> result = assetService.listAssets(customerId, null, null);

        // Then
        assertEquals(2, result.size()); // All assets should be returned
        verify(assetRepository, times(1)).findByCustomerId(customerId);
    }

}


