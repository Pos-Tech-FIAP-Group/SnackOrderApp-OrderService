package com.fiap.snackapp.core.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderItemsRequest(
        @NotEmpty(message = "A lista de itens não pode estar vazia")
        @Valid
        List<ItemRequest> items
) { }