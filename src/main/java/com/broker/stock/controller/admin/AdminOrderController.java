package com.broker.stock.controller.admin;

import com.broker.stock.model.OrderResponse;
import com.broker.stock.service.business.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PostMapping("/match-orders")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<OrderResponse>> matchOrders() {
        List<OrderResponse> matchResponses = orderService.matchOrders();
        return ResponseEntity.status(HttpStatus.OK).body(matchResponses);
    }
}
