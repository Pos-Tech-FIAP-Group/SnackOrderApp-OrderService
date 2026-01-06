package com.fiap.snackapp.core.application.dto.response;

import com.fiap.snackapp.core.domain.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderPaymentCreatedMessageResponse(
        String paymentId,
        Long orderId,
        BigDecimal amount,
        String qrCodeUrl,
        OrderStatus status
) {
}
