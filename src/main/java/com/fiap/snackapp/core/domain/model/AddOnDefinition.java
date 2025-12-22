package com.fiap.snackapp.core.domain.model;

import com.fiap.snackapp.core.domain.enums.Category;

import java.math.BigDecimal;

public record AddOnDefinition(
        Long id,
        String name,
        Category category,
        BigDecimal price,
        boolean active
) {
    public AddOnDefinition(Long id, String name, Category category, BigDecimal price) {
        this(id, name, category, price, true);
    }

}