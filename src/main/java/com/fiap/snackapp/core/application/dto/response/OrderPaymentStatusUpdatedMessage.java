package com.fiap.snackapp.core.application.dto.response;

import com.fiap.snackapp.core.domain.enums.OrderStatus;

public record OrderPaymentStatusUpdatedMessage(
        Long orderId,
        String paymentId,
        OrderStatus status
) {
}
