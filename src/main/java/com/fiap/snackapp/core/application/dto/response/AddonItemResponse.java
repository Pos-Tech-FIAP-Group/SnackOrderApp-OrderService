package com.fiap.snackapp.core.application.dto.response;

import com.fiap.snackapp.core.domain.enums.Category;

import java.math.BigDecimal;


public record AddonItemResponse(
        Long id,
        String name,
        Category category,
        BigDecimal price,
        Integer quantity
) {}