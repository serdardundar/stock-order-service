package com.broker.stock.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.broker.stock.constant.OrderSide;
import com.broker.stock.constant.OrderStatus;
import com.broker.stock.controller.business.OrderController;
import com.broker.stock.model.OrderRequest;
import com.broker.stock.model.OrderResponse;
import com.broker.stock.service.business.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateOrder_Successful() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest(1L, "GOLD", OrderSide.BUY, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
        OrderResponse orderResponse = new OrderResponse(1L, 1L, "GOLD", OrderSide.BUY, OrderStatus.PENDING, LocalDateTime.now());

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When/Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.assetName").value("GOLD"))
            .andExpect(jsonPath("$.orderSide").value("BUY"));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void testCreateOrder_InvalidRequest() throws Exception {
        // Given
        OrderRequest invalidRequest = new OrderRequest(null, "", null, null, null); // Invalid request

        // When/Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testListOrders_Successful() throws Exception {
        // Given
        Long customerId = 1L;
        String startDate = "2025-03-01T12:00:00";
        String endDate = "2025-03-10T12:00:00";

        OrderResponse order1 = new OrderResponse(1L, customerId, "GOLD", OrderSide.BUY, OrderStatus.PENDING, LocalDateTime.parse("2025-03-03T10:00:00"));
        OrderResponse order2 = new OrderResponse(2L, customerId, "SILVER", OrderSide.SELL, OrderStatus.MATCHED, LocalDateTime.parse("2025-03-05T14:00:00"));

        when(orderService.listOrders(customerId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)))
            .thenReturn(List.of(order1, order2));

        // When/Then
        mockMvc.perform(get("/api/orders")
                .param("customerId", customerId.toString())
                .param("startDate", startDate)
                .param("endDate", endDate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].assetName").value("GOLD"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].assetName").value("SILVER"));

        verify(orderService, times(1)).listOrders(customerId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Test
    void testListOrders_InvalidDateFormat() throws Exception {
        // Given
        Long customerId = 1L;
        String invalidStartDate = "invalid-date";
        String endDate = "2025-03-10T12:00:00";

        // When/Then
        mockMvc.perform(get("/api/orders")
                .param("customerId", Long.toString(customerId))
                .param("startDate", invalidStartDate)
                .param("endDate", endDate))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("Invalid date format")));
    }

    @Test
    void testListOrders_StartDateAfterEndDate() throws Exception {
        // Given
        Long customerId = 1L;
        String startDate = "2025-03-10T12:00:00";
        String endDate = "2025-03-01T12:00:00"; // Start date is after end date

        // When/Then
        mockMvc.perform(get("/api/orders")
                .param("customerId", customerId.toString())
                .param("startDate", startDate)
                .param("endDate", endDate))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("Start date cannot be after end date")));
    }

    @Test
    void testDeleteOrder_Successful() throws Exception {
        // Given
        Long customerId = 1L;
        Long orderId = 10L;

        doNothing().when(orderService).deleteOrder(customerId, orderId);

        // When/Then
        mockMvc.perform(delete("/api/orders/{customerId}/orders/{orderId}", customerId, orderId))
            .andExpect(status().isOk())
            .andExpect(content().string("Order successfully canceled and relevant balances updated."));

        verify(orderService, times(1)).deleteOrder(customerId, orderId);
    }
}
