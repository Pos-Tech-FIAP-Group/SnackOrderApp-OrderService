package com.fiap.snackapp.core.application.dto.request;

public record AddOnRequest(
        Long addOnId,
        int quantity
) {}