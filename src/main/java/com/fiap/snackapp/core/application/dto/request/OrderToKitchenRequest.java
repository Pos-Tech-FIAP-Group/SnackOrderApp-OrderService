package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderToKitchenRequest(
        @NotNull
        Long orderId,
        @NotEmpty
        List<ItemToKitchenRequest> itens
) {
}
