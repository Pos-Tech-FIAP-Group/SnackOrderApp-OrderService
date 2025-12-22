package com.fiap.snackapp.core.application.dto.response;

import com.fiap.snackapp.core.domain.enums.Category;

import java.math.BigDecimal;

public record AddOnResponse(
        Long id,
        String name,
        Category category,
        BigDecimal price,
        boolean active
) {}