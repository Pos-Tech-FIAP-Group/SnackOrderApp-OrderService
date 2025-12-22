package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ItemRequest(
        @NotNull
        Long productId,
        @Min(1)
        int quantity,
        List<AddOnRequest> addOns

) { }