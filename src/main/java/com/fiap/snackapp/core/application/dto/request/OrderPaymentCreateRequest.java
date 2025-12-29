package com.fiap.snackapp.core.application.dto.request;

import java.math.BigDecimal;

public record OrderPaymentCreateRequest(
        Long orderId,
        BigDecimal amount,
        Long customerId
) {
}
