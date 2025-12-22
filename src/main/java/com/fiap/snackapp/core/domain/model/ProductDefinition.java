package com.fiap.snackapp.core.domain.model;

import com.fiap.snackapp.core.domain.enums.Category;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;


public record ProductDefinition (
        Long id,
        String name,
        Category category,
        BigDecimal price,
        String description,
        boolean active
) implements Product {

    public ProductDefinition(String name, Category category, BigDecimal price, String description) {
        this(null, name, category, price, description, true);
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public ProductDefinition getProductDefinition() {
        return this;
    }

    @Override
    public List<AppliedAddOn> getAppliedAddOns() {
        return Collections.emptyList();
    }
}