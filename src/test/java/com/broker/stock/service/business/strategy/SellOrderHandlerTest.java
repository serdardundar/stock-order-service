package com.broker.stock.service.business.strategy;

import com.broker.stock.constant.OrderSide;
import com.broker.stock.entity.Asset;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.service.business.AssetService;
import com.broker.stock.service.business.strategy.SellOrderHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SellOrderHandlerTest {

    @Mock
    private AssetService assetService;

    @InjectMocks
    private SellOrderHandler sellOrderHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void handleOrder_SuccessfulExecution() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal size = BigDecimal.valueOf(5);
        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.SELL, size, BigDecimal.valueOf(100));

        Asset asset = new Asset(1L, customerId, assetName, BigDecimal.valueOf(10), BigDecimal.valueOf(10)); // Sufficient balance

        // Mock behavior
        when(assetService.getAsset(customerId, assetName)).thenReturn(Optional.of(asset));

        // When
        sellOrderHandler.handleOrder(orderRequest);

        // Then
        verify(assetService).save(asset); // Ensure the asset was saved
        assert asset.getUsableSize().equals(BigDecimal.valueOf(5)); // Check updated size
    }

    @Test
    void handleOrder_InsufficientAssetBalance() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal size = BigDecimal.valueOf(15); // Attempting to sell more than available
        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.SELL, size, BigDecimal.valueOf(100));

        Asset asset = new Asset(1L, customerId, assetName, BigDecimal.valueOf(10), BigDecimal.valueOf(10)); // Insufficient balance

        // Mock behavior
        when(assetService.getAsset(customerId, assetName)).thenReturn(Optional.of(asset));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> sellOrderHandler.handleOrder(orderRequest));
        verify(assetService, never()).save(any()); // Ensure the asset was not saved
    }

    @Test
    void handleOrder_MissingAsset() {
        // Given
        Long customerId = 1L;
        String assetName = "GOLD";
        BigDecimal size = BigDecimal.valueOf(5);
        OrderRequest orderRequest = new OrderRequest(customerId, assetName, OrderSide.SELL, size, BigDecimal.valueOf(100));

        // Mock behavior
        when(assetService.getAsset(customerId, assetName)).thenReturn(Optional.empty()); // Asset does not exist

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> sellOrderHandler.handleOrder(orderRequest));
        verify(assetService, never()).save(any()); // Ensure the asset was not saved
    }
}

