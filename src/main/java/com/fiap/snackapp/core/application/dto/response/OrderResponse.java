package com.fiap.snackapp.core.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        String cpf,
        List<OrderItemResponse> items,
        BigDecimal totalPrice
) { }