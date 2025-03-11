package com.broker.stock.service.business.strategy;

import static com.broker.stock.constant.AssetConstants.TRY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.broker.stock.constant.OrderSide;
import com.broker.stock.entity.Asset;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.service.business.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

class BuyOrderHandlerTest {

    @Mock
    private AssetService assetService;

    @InjectMocks
    private BuyOrderHandler buyOrderHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void handleOrder_SuccessfulExecution() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal price = BigDecimal.valueOf(100);
        BigDecimal size = BigDecimal.TEN;
        BigDecimal requiredTRY = size.multiply(price); // 1000 TRY required

        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.BUY, size, price);
        Asset tryAsset = new Asset(1L, customerId, TRY, requiredTRY.add(BigDecimal.valueOf(500)), requiredTRY.add(BigDecimal.valueOf(500))); // Sufficient TRY balance
        Asset orderAsset = new Asset(2L, customerId, assetName, BigDecimal.ZERO, BigDecimal.ZERO); // Existing asset

        // Mock behavior
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, assetName)).thenReturn(Optional.of(orderAsset));

        // When
        buyOrderHandler.handleOrder(orderRequest);

        // Then
        verify(assetService).save(tryAsset); // TRY asset was updated
        verify(assetService, never()).save(orderAsset); // Order asset already exists; no initialization
        assert tryAsset.getUsableSize().compareTo(BigDecimal.valueOf(500)) == 0; // 500 TRY remains usable
    }

    @Test
    void handleOrder_InitializeAsset() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal price = BigDecimal.valueOf(100);
        BigDecimal size = BigDecimal.TEN;
        BigDecimal requiredTRY = size.multiply(price); // 1000 TRY required

        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.BUY, size, price);
        Asset tryAsset = new Asset(1L, customerId, TRY, requiredTRY.add(BigDecimal.valueOf(500)), requiredTRY.add(BigDecimal.valueOf(500))); // Sufficient TRY balance

        // Mock behavior
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, assetName)).thenReturn(Optional.empty()); // Asset does not exist

        // When
        buyOrderHandler.handleOrder(orderRequest);

        // Then
        verify(assetService).save(tryAsset); // TRY asset was updated
        verify(assetService).save(argThat(asset -> // Verify asset initialization
            asset.getCustomerId().equals(customerId)
                && asset.getAssetName().equals(assetName)
                && asset.getSize().equals(BigDecimal.ZERO)
                && asset.getUsableSize().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    void handleOrder_InsufficientTRYBalance() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal price = BigDecimal.valueOf(100);
        BigDecimal size = BigDecimal.TEN;

        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.BUY, size, price);
        Asset tryAsset = new Asset(1L, customerId, TRY, BigDecimal.valueOf(500), BigDecimal.ZERO); // Insufficient TRY balance

        // Mock behavior
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));

        // When/Then
        assertThrows(IllegalStateException.class, () -> buyOrderHandler.handleOrder(orderRequest));
        verify(assetService, never()).save(tryAsset); // TRY asset should not be saved
    }

    @Test
    void handleOrder_MissingTRYAsset() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal price = BigDecimal.valueOf(100);
        BigDecimal size = BigDecimal.TEN;

        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.BUY, size, price);

        // Mock behavior
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.empty()); // TRY asset does not exist

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> buyOrderHandler.handleOrder(orderRequest));
        verify(assetService, never()).save(any()); // No asset should be saved
    }
}
