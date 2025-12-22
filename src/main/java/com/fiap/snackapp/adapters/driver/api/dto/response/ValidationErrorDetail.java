package com.fiap.snackapp.adapters.driver.api.dto.response;

public record ValidationErrorDetail(
        String field,
        String message
) {}