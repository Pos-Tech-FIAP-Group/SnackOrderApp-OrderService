package com.fiap.snackapp.core.application.dto.request;

import com.fiap.snackapp.core.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "O status é obrigatório")
        OrderStatus status
) {}