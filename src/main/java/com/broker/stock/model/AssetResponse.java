package com.broker.stock.model;

import java.math.BigDecimal;

public record AssetResponse(Long customerId,
                            String assetName,
                            BigDecimal size,
                            BigDecimal usableSize) {
}
