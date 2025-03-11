package com.broker.stock.controller.business;

import com.broker.stock.model.AssetResponse;
import com.broker.stock.service.business.AssetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<AssetResponse>> listAssets(
        @RequestParam Long customerId,
        @RequestParam(required = false) String assetName,
        @RequestParam(required = false) BigDecimal minUsableSize) {

        List<AssetResponse> assets = assetService.listAssets(customerId, assetName, minUsableSize);

        return ResponseEntity.ok(assets);
    }
}
