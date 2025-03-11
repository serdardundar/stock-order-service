package com.broker.stock.service.business;

import static com.broker.stock.constant.AssetConstants.TRY;
import com.broker.stock.aspect.AuthorizeCustomer;
import com.broker.stock.aspect.CustomerId;
import com.broker.stock.constant.OrderSide;
import com.broker.stock.constant.OrderStatus;
import com.broker.stock.entity.Asset;
import com.broker.stock.entity.Order;
import com.broker.stock.mapper.OrderMapper;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.model.OrderResponse;
import com.broker.stock.repository.OrderRepository;
import com.broker.stock.service.business.strategy.OrderHandler;
import com.broker.stock.service.business.strategy.OrderHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderHandlerFactory handlerFactory;
    private final OrderRepository orderRepository;
    private final AssetService assetService;

    /**
     * Creates a new order and updates assets accordingly.
     *
     * @param orderRequest the request containing order details
     * @return the created order as a response
     */
    @Transactional
    @AuthorizeCustomer
    public OrderResponse createOrder(OrderRequest orderRequest) {
        validateOrderRequest(orderRequest);

        OrderHandler orderHandler = handlerFactory.getHandler(orderRequest.orderSide());
        orderHandler.handleOrder(orderRequest);

        var order = OrderMapper.toEntity(orderRequest);
        var savedOrder = orderRepository.save(order);

        return OrderMapper.toResponse(savedOrder);
    }

    /**
     * Lists orders within a given date range for a customer.
     *
     * @param customerId the customer ID
     * @param startDate  the start date for the query
     * @param endDate    the end date for the query
     * @return a list of order responses
     */
    @AuthorizeCustomer
    public List<OrderResponse> listOrders(@CustomerId Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate)
            .stream()
            .map(OrderMapper::toResponse)
            .toList();
    }

    /**
     * Deletes an order and reverses its effects on assets.
     *
     * @param customerId the customer ID
     * @param orderId    the order ID
     * @throws IllegalAccessException if the customer is unauthorized to delete the order
     */
    @Transactional
    @AuthorizeCustomer
    public void deleteOrder(@CustomerId Long customerId, Long orderId) throws IllegalAccessException {
        log.info("Deleting order with id {}, for customer {}", orderId, customerId);

        var order = findOrderById(orderId);
        validateOrderOwnership(order, customerId);
        validateOrderStatus(order);

        reverseOrderEffects(order);
        markOrderAsCancelled(order);

        log.info("Order with id {} successfully canceled for customer {}", orderId, customerId);
    }

    /**
     * Matches the orders and updates assets accordingly.
     *
     * @return matched order list as a response
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> matchOrders() {
        return orderRepository.findByStatusOrderById(OrderStatus.PENDING)
            .stream()
            .peek(order -> log.info("Matching Order: {}", order))
            .map(this::handlePendingOrder)
            .peek(response -> log.info("Matched Order: {}", response))
            .toList();
    }

    // --- HANDLER METHODS ---

    private OrderResponse handlePendingOrder(Order order) {
        Asset tryAsset = getValidatedAsset(order.getCustomerId(), TRY);
        Asset orderAsset = getValidatedAsset(order.getCustomerId(), order.getAssetName());

        BigDecimal tryChangeValue = calculateChangeValue(order);

        if (order.getOrderSide() == OrderSide.BUY) {
            updateAssetsForBuyOrder(tryAsset, orderAsset, order.getSize(), tryChangeValue);
        } else if (order.getOrderSide() == OrderSide.SELL) {
            updateAssetsForSellOrder(tryAsset, orderAsset, order.getSize(), tryChangeValue);
        }

        order.setStatus(OrderStatus.MATCHED);
        Order savedOrder = orderRepository.save(order);

        return OrderMapper.toResponse(savedOrder);
    }

    private void updateAssetsForBuyOrder(Asset tryAsset, Asset orderAsset, BigDecimal size, BigDecimal tryChangeValue) {
        tryAsset.setSize(tryAsset.getSize().subtract(tryChangeValue));
        assetService.save(tryAsset);

        orderAsset.setSize(orderAsset.getSize().add(size));
        orderAsset.setUsableSize(orderAsset.getUsableSize().add(size));
        assetService.save(orderAsset);
    }

    private void updateAssetsForSellOrder(Asset tryAsset, Asset orderAsset, BigDecimal size, BigDecimal tryChangeValue) {
        tryAsset.setSize(tryAsset.getSize().add(tryChangeValue));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(tryChangeValue));
        assetService.save(tryAsset);

        orderAsset.setSize(orderAsset.getSize().subtract(size));
        assetService.save(orderAsset);
    }

    private BigDecimal calculateChangeValue(Order order) {
        return order.getSize().multiply(order.getPrice());
    }

    private Asset getValidatedAsset(Long customerId, String assetName) {
        return assetService.getAsset(customerId, assetName)
            .orElseThrow(() -> new IllegalArgumentException(String.format("Asset %s not found for customerId %d", assetName, customerId)));
    }

    // --- VALIDATION METHODS ---
    private void validateOrderRequest(OrderRequest orderRequest) {
        var tryAsset = assetService.getAsset(orderRequest.customerId(), TRY);

        if (tryAsset.isEmpty() && orderRequest.orderSide() == OrderSide.BUY) {
            throw new IllegalArgumentException("Missing TRY balance for BUY order.");
        }

        if (tryAsset.isPresent() && orderRequest.assetName().equals(TRY)) {
            throw new IllegalArgumentException("TRY order is invalid.");
        }
    }

    private void validateOrderOwnership(Order order, Long customerId) throws IllegalAccessException {
        if (!order.getCustomerId().equals(customerId)) {
            throw new IllegalAccessException("Unauthorized: Access Denied.");
        }
    }

    private void validateOrderStatus(Order order) {
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalArgumentException("Only PENDING orders can be canceled.");
        }
    }

    private void reverseOrderEffects(Order order) {
        var tryAsset = getValidatedAsset(order.getCustomerId(), TRY);
        var asset = getValidatedAsset(order.getCustomerId(), order.getAssetName());

        BigDecimal size = order.getSize();
        BigDecimal value = calculateChangeValue(order);

        if (order.getOrderSide() == OrderSide.BUY) {
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(value));
            assetService.save(tryAsset);
        } else if (order.getOrderSide() == OrderSide.SELL) {
            asset.setUsableSize(asset.getUsableSize().add(size));
            assetService.save(asset);
        }
    }

    private void markOrderAsCancelled(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
    }
}

