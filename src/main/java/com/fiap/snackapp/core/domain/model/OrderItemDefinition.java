package com.fiap.snackapp.core.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemDefinition(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        List<AppliedAddOn> appliedAddOns
) {
    public BigDecimal getTotalPrice() {

        BigDecimal addOnsTotal = appliedAddOns.stream()
                .map(a -> a.getAddOnDefinition().price().multiply(BigDecimal.valueOf(a.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return price.add(addOnsTotal).multiply(BigDecimal.valueOf(quantity));
    }
}