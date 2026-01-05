package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ItemToKitchenRequest(
        @NotBlank
        String name,
        @Min(1)
        int quantity,
        List<AddOnToKitchenRequest> addOns

) { }