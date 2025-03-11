package com.broker.stock.controller.business;

import com.broker.stock.model.OrderRequest;
import com.broker.stock.model.OrderResponse;
import com.broker.stock.service.business.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        var orderResponse = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<OrderResponse>> listOrders(
        @RequestParam Long customerId,
        @RequestParam String startDate,
        @RequestParam String endDate) {

        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(startDate);
            end = LocalDateTime.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use ISO_LOCAL_DATE_TIME (e.g., 2025-01-01:00:00)");
        }

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
        }

        List<OrderResponse> orders = orderService.listOrders(customerId, start, end);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{customerId}/orders/{orderId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> deleteOrder(@PathVariable Long customerId,
                                              @PathVariable Long orderId) throws IllegalAccessException {
        orderService.deleteOrder(customerId, orderId);
        return ResponseEntity.ok("Order successfully canceled and relevant balances updated.");
    }
}
