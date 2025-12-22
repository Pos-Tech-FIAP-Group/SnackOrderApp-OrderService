package com.fiap.snackapp.core.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DecoratedProductResponse(
        Long productId,
        String description,
        BigDecimal finalPrice,
        ProductResponse product,
        List<AppliedAddOnResponse> appliedAddOns
) {}