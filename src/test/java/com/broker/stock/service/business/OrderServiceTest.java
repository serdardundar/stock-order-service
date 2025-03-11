package com.broker.stock.service.business;

import static com.broker.stock.constant.AssetConstants.TRY;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.broker.stock.constant.OrderSide;
import com.broker.stock.constant.OrderStatus;
import com.broker.stock.entity.Asset;
import com.broker.stock.entity.Order;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.model.OrderResponse;
import com.broker.stock.repository.OrderRepository;
import com.broker.stock.service.business.AssetService;
import com.broker.stock.service.business.OrderService;
import com.broker.stock.service.business.strategy.BuyOrderHandler;
import com.broker.stock.service.business.strategy.OrderHandlerFactory;
import com.broker.stock.service.business.strategy.SellOrderHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private OrderHandlerFactory orderHandlerFactory;

    @InjectMocks
    private OrderService orderService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(orderHandlerFactory.getHandler(OrderSide.BUY)).thenReturn(new BuyOrderHandler(assetService));
        when(orderHandlerFactory.getHandler(OrderSide.SELL)).thenReturn(new SellOrderHandler(assetService));
    }

    @Test
    @DisplayName("BUY Order is on PENDING and new Asset is created")
    void testBuyOrder_ValidScenario_NewAssetCreation() {
        // Given
        Long customerId = 1L;
        OrderRequest orderRequest = new OrderRequest(customerId, "GOLD", OrderSide.BUY, BigDecimal.valueOf(10), BigDecimal.valueOf(100));

        Order mockOrder = mock(Order.class);

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(10000));

        // Mock TRY asset availability
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, "GOLD")).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn(1L);
        // When
        orderService.createOrder(orderRequest);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        // Verify order model is saved correctly
        assertAll(() -> {
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
            assertEquals(OrderSide.BUY, savedOrder.getOrderSide());
            assertEquals(BigDecimal.valueOf(10), savedOrder.getSize());
            assertEquals(BigDecimal.valueOf(100), savedOrder.getPrice());
        });

        // Then
        // Verify Saved Asset count
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetService, times(2)).save(assetCaptor.capture());

        Asset updatedTRYAsset = assetCaptor.getAllValues().stream()
            .filter(asset -> asset.getAssetName().equals(TRY))
            .findFirst()
            .orElseThrow();

        Asset createdGoldAsset = assetCaptor.getAllValues().stream()
            .filter(asset -> asset.getAssetName().equals("GOLD"))
            .findFirst()
            .orElseThrow();

        // Verify TRY asset update
        assertEquals(BigDecimal.valueOf(9000), updatedTRYAsset.getUsableSize()); // 10000 - (10 * 100)

        // Verify GOLD asset creation
        assertEquals("GOLD", createdGoldAsset.getAssetName());
        assertEquals(BigDecimal.ZERO, createdGoldAsset.getSize());
        assertEquals(BigDecimal.ZERO, createdGoldAsset.getUsableSize());
    }

    @Test()
    @DisplayName("BUY Order is on PENDING and no change on asset only TRY asset updated")
    void testBuyOrder_ValidScenario_UpdateOnlyTRYAsset() {
        // Given
        Long customerId = 1L;
        OrderRequest orderRequest = new OrderRequest(customerId, "GOLD", OrderSide.BUY, BigDecimal.valueOf(5), BigDecimal.valueOf(100));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(10000));

        Asset goldAsset = new Asset();
        goldAsset.setCustomerId(customerId);
        goldAsset.setAssetName("GOLD");
        goldAsset.setSize(BigDecimal.valueOf(20));
        goldAsset.setUsableSize(BigDecimal.valueOf(20));

        Order mockOrder = mock(Order.class);

        // Mock asset availability
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, "GOLD")).thenReturn(Optional.of(goldAsset));
        when(orderRepository.save(any())).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn(1L);

        orderService.createOrder(orderRequest);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Verify order model is saved correctly
        assertAll(() -> {
            verify(orderRepository, times(1)).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
            assertEquals(OrderSide.BUY, savedOrder.getOrderSide());
        });

        // Verify Saved Asset count
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetService, times(1)).save(assetCaptor.capture());

        // Validate the TRY asset update
        Asset updatedTRYAsset = assetCaptor.getValue();
        assertEquals(BigDecimal.valueOf(9500), updatedTRYAsset.getUsableSize()); // TRY usable size is updated
    }

    @Test
    @DisplayName("SELL Order is on PENDING and no change on TRY only asset updated")
    void testSellOrder_ValidScenario() {
        // Given
        Long customerId = 1L;
        OrderRequest orderRequest = new OrderRequest(customerId, "GOLD", OrderSide.SELL, BigDecimal.valueOf(5), BigDecimal.valueOf(200));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(10000));

        Asset goldAsset = new Asset();
        goldAsset.setCustomerId(customerId);
        goldAsset.setAssetName("GOLD");
        goldAsset.setSize(BigDecimal.valueOf(20));
        goldAsset.setUsableSize(BigDecimal.valueOf(20));

        Order mockOrder = mock(Order.class);

        // Mock asset availability
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, "GOLD")).thenReturn(Optional.of(goldAsset));
        when(orderRepository.save(any())).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn(1L);

        // When
        orderService.createOrder(orderRequest);

        // Then
        // Capture both calls to assetService.save()
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetService, times(1)).save(assetCaptor.capture());

        // Validate the GOLD asset update
        Asset updatedGoldAsset = assetCaptor.getValue();
        assertEquals(BigDecimal.valueOf(20), updatedGoldAsset.getSize()); // 20 - 5
        assertEquals(BigDecimal.valueOf(15), updatedGoldAsset.getUsableSize());
    }

    @Test
    @DisplayName("BUY order is failed for insufficient TRY balance")
    void testBuyOrder_InvalidScenario_InsufficientTRYBalance() {
        // Given
        Long customerId = 1L;
        OrderRequest orderRequest = new OrderRequest(customerId, "GOLD", OrderSide.BUY, BigDecimal.valueOf(20), BigDecimal.valueOf(100));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(1500));
        tryAsset.setUsableSize(BigDecimal.valueOf(1500));

        // Mock TRY asset availability
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));

        // When/Then
        Exception exception = assertThrows(IllegalStateException.class, () -> orderService.createOrder(orderRequest));
        assertEquals("Insufficient TRY balance.", exception.getMessage());

        verify(assetService, never()).save(any());
    }

    @Test
    @DisplayName("SELL order is failed for insufficient asset balance")
    void testSellOrder_InvalidScenario_InsufficientAssetBalance() {
        // Given
        Long customerId = 1L;
        OrderRequest orderRequest = new OrderRequest(customerId, "GOLD", OrderSide.SELL, BigDecimal.valueOf(25), BigDecimal.valueOf(200));

        Asset goldAsset = new Asset();
        goldAsset.setCustomerId(customerId);
        goldAsset.setAssetName("GOLD");
        goldAsset.setSize(BigDecimal.valueOf(20));
        goldAsset.setUsableSize(BigDecimal.valueOf(20));

        // Mock GOLD asset availability
        when(assetService.getAsset(customerId, "GOLD")).thenReturn(Optional.of(goldAsset));

        // When/Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequest));
        assertEquals("Insufficient asset balance.", exception.getMessage());

        verify(assetService, never()).save(any());
    }

    @Test
    @DisplayName("BUY Order is CANCELLED and change on TRY and no change on asset")
    void testDeleteOrder_ValidScenario_ChangeOnTRYNotAsset() throws IllegalAccessException {
        // Given
        Long customerId = 1L;
        Long orderId = 100L;

        // Mock an order (PENDING status)
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setAssetName("GOLD");
        order.setOrderSide(OrderSide.BUY);
        order.setStatus(OrderStatus.PENDING);
        order.setSize(BigDecimal.valueOf(5));
        order.setPrice(BigDecimal.valueOf(200));

        // Mock TRY asset
        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(9000));

        // Mock GOLD asset
        Asset goldAsset = new Asset();
        goldAsset.setCustomerId(customerId);
        goldAsset.setAssetName("GOLD");
        goldAsset.setSize(BigDecimal.valueOf(15));
        goldAsset.setUsableSize(BigDecimal.valueOf(15));

        // Mock dependencies
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, "GOLD")).thenReturn(Optional.of(goldAsset));

        // When
        orderService.deleteOrder(customerId, orderId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Verify order model is saved correctly
        assertAll(() -> {
            verify(orderRepository, times(1)).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertEquals(OrderStatus.CANCELLED, savedOrder.getStatus());
        });

        // Then
        // Capture reversed TRY asset
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetService, times(1)).save(assetCaptor.capture());


        Asset updatedTRYAsset = assetCaptor.getValue();
        assertEquals(BigDecimal.valueOf(10000), updatedTRYAsset.getSize()); // no change
        assertEquals(BigDecimal.valueOf(10000), updatedTRYAsset.getUsableSize()); // Reversed to original usable size
    }

    @Test
    @DisplayName("SELL Order is CANCELLED and change on asset and no change on TRY")
    void testDeleteOrder_ValidScenario_ChangeOnAssetNotTRY() throws IllegalAccessException {
        // Given
        Long customerId = 1L;
        Long orderId = 100L;

        // Mock an order (PENDING status)
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setAssetName("GOLD");
        order.setOrderSide(OrderSide.SELL);
        order.setStatus(OrderStatus.PENDING);
        order.setSize(BigDecimal.valueOf(5));
        order.setPrice(BigDecimal.valueOf(200));

        // Mock TRY asset
        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(customerId);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(9000));

        // Mock GOLD asset
        Asset goldAsset = new Asset();
        goldAsset.setCustomerId(customerId);
        goldAsset.setAssetName("GOLD");
        goldAsset.setSize(BigDecimal.valueOf(15));
        goldAsset.setUsableSize(BigDecimal.valueOf(15));

        // Mock dependencies
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetService.getAsset(customerId, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(customerId, "GOLD")).thenReturn(Optional.of(goldAsset));

        // When
        orderService.deleteOrder(customerId, orderId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Verify order model is saved correctly
        assertAll(() -> {
            verify(orderRepository, times(1)).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertEquals(OrderStatus.CANCELLED, savedOrder.getStatus());
        });

        // Then
        // Capture reversed TRY asset
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetService, times(1)).save(assetCaptor.capture());


        Asset updatedAsset = assetCaptor.getValue();
        assertEquals(BigDecimal.valueOf(15), updatedAsset.getSize()); // no change
        assertEquals(BigDecimal.valueOf(20), updatedAsset.getUsableSize()); // Reversed to original usable size
    }

    @Test
    @DisplayName("Orders are successfully listed")
    void testListOrders_Successful() {
        // Given
        Long customerId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCustomerId(customerId);
        order1.setAssetName("GOLD");
        order1.setOrderSide(OrderSide.BUY);
        order1.setStatus(OrderStatus.PENDING);
        order1.setSize(BigDecimal.valueOf(10));
        order1.setPrice(BigDecimal.valueOf(100));
        order1.setCreateDate(LocalDateTime.of(2025, 3, 15, 10, 30));

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerId(customerId);
        order2.setAssetName("SILVER");
        order2.setOrderSide(OrderSide.SELL);
        order2.setStatus(OrderStatus.MATCHED);
        order2.setSize(BigDecimal.valueOf(5));
        order2.setPrice(BigDecimal.valueOf(50));
        order2.setCreateDate(LocalDateTime.of(2025, 6, 10, 14, 0));

        List<Order> mockOrderList = List.of(order1, order2);

        // Mock repository behavior
        when(orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate)).thenReturn(mockOrderList);

        // When
        List<OrderResponse> orderResponses = orderService.listOrders(customerId, startDate, endDate);

        // Then
        assertEquals(2, orderResponses.size()); // Ensure two orders are returned

        // Validate order1 details
        OrderResponse response1 = orderResponses.get(0);
        assertAll(() -> {
            assertEquals(1L, response1.id());
            assertEquals("GOLD", response1.assetName());
            assertEquals(OrderSide.BUY, response1.orderSide());
            assertEquals(OrderStatus.PENDING, response1.status());
            assertEquals(LocalDateTime.of(2025, 3, 15, 10, 30), response1.createDate());
        });

        // Validate order2 details
        OrderResponse response2 = orderResponses.get(1);
        assertAll(() -> {
            assertEquals(2L, response2.id());
            assertEquals("SILVER", response2.assetName());
            assertEquals(OrderSide.SELL, response2.orderSide());
            assertEquals(OrderStatus.MATCHED, response2.status());
            assertEquals(LocalDateTime.of(2025, 6, 10, 14, 0), response2.createDate());
        });

        // Verify repository interaction
        verify(orderRepository, times(1)).findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Test
    void testMatchOrders_Successful() {
        // Given
        Order buyOrder1 = createOrder(1L, 1L, "GOLD", OrderSide.BUY, BigDecimal.valueOf(10), BigDecimal.valueOf(100)); // BUY size 1000
        Order buyOrder2 = createOrder(2L, 1L, "GOLD", OrderSide.BUY, BigDecimal.valueOf(5), BigDecimal.valueOf(100)); // BUY size 500
        Order sellOrder = createOrder(3L, 1L, "GOLD", OrderSide.SELL, BigDecimal.valueOf(8), BigDecimal.valueOf(100)); // SELL size 800
        Asset tryAsset = createAsset(1L, TRY, BigDecimal.valueOf(10000), BigDecimal.valueOf(9200));
        Asset goldAsset = createAsset(1L, "GOLD", BigDecimal.valueOf(10), BigDecimal.valueOf(10));

        // Mock repository behavior
        when(orderRepository.findByStatusOrderById(OrderStatus.PENDING)).thenReturn(List.of(buyOrder1, buyOrder2, sellOrder));
        when(assetService.getAsset(1L, TRY)).thenReturn(Optional.of(tryAsset));
        when(assetService.getAsset(1L, "GOLD")).thenReturn(Optional.of(goldAsset));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0)); // Save returns the order itself

        // When
        List<OrderResponse> responses = orderService.matchOrders();

        // Then
        assertEquals(3, responses.size());
        assertEquals(OrderStatus.MATCHED, buyOrder1.getStatus());
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);

        verify(assetService, times(6)).save(assetCaptor.capture()); // TRY and GOLD assets updated
        Asset updatedTRYAsset = assetCaptor.getAllValues().stream().filter(asset -> asset.getAssetName().equals(TRY)).findFirst().orElseThrow();
        assertEquals(BigDecimal.valueOf(9300), updatedTRYAsset.getSize());// (10000 - 1000 - 500 + 800)
        assertEquals(BigDecimal.valueOf(10000), updatedTRYAsset.getUsableSize());

        verify(orderRepository, times(1)).save(buyOrder1);
        verify(orderRepository, times(1)).save(buyOrder2);
        verify(orderRepository, times(1)).save(sellOrder);
    }

    // Helper to create test Order objects
    private Order createOrder(Long id, Long customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderSide(side);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    // Helper to create test Asset objects
    private Asset createAsset(Long customerId, String assetName, BigDecimal size, BigDecimal usableSize) {
        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName(assetName);
        asset.setSize(size);
        asset.setUsableSize(usableSize);
        return asset;
    }
}

