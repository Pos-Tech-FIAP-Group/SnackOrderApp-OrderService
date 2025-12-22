package com.fiap.snackapp.core.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal productPrice,
        int quantity,
        List<AddonItemResponse> addOns,
        BigDecimal totalPrice
) { }