package com.broker.stock.service.business;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class OrderServiceAuthorizationIntegrationTest {


    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private String validJwtToken; // Simulated token

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Simulate a valid JWT token for testing
        validJwtToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc0MTU5NzgxNywiZXhwIjoxNzQxNjMzODE3fQ.1idu9YvcFQwo5maGY1p87LzHbki3anI38DwegynmtCw";
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN", password = "admin123")
    void testListOrders_WithAdminByPass() throws Exception {
        // Given
        Long customerId = 1L;
        String startDate = "2025-01-01T00:00:00";
        String endDate = "2025-12-31T23:59:59";

        // When/Then
        mockMvc.perform(get("/api/orders")
                .param("customerId", customerId.toString())
                .param("startDate", startDate)
                .param("endDate", endDate)
                .header(HttpHeaders.AUTHORIZATION, validJwtToken)) // Simulate passing a valid JWT token
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer2", roles = "USER", password = "password234")
    void testDeleteOrder_BadRequest_NoToken() throws Exception {
        // Given
        Long customerId = 1L;
        Long orderId = 10L;

        // When/Then
        mockMvc.perform(delete("/api/orders/{customerId}/orders/{orderId}", customerId, orderId))
            .andExpect(status().isBadRequest()); // Should fail since no token is provided
    }
}
