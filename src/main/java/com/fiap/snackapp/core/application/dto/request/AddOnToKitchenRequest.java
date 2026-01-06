package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;


public record AddOnToKitchenRequest(
        @NotBlank
        String name,
        @Min(1)
        int quantity
) {
}

