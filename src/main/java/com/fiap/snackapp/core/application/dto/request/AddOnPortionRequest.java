package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddOnPortionRequest(
        @NotNull
        Long addOnId,
        @Min(1)
        int quantity
) {}