package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProductCustomizationRequest(
        @NotNull
        Long productId,
        List<@Valid AddOnPortionRequest> addOnPortions
) {}