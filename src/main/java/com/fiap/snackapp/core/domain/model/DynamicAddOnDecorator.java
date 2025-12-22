package com.fiap.snackapp.core.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record DynamicAddOnDecorator(
        Product decoratedProduct,
        AddOnDefinition addOn,
        int quantity
) implements Product {
    public DynamicAddOnDecorator(Product decoratedProduct, AddOnDefinition addOn, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A quantidade do adicional deve ser positiva.");
        }
        this.decoratedProduct = Objects.requireNonNull(decoratedProduct);
        this.addOn = Objects.requireNonNull(addOn);
        this.quantity = quantity;
    }

    @Override
    public Long getId() {
        return decoratedProduct.getId();
    }

    @Override
    public String getName() {
        String quantitySuffix = quantity > 1 ? " (x" + quantity + ")" : "";
        return decoratedProduct.getDescription() + ", com " + addOn.name() + quantitySuffix;
    }

    @Override
    public String getDescription() {
        return decoratedProduct.getDescription();
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal addOnTotalCost = addOn.price().multiply(BigDecimal.valueOf(this.quantity));
        return decoratedProduct.getPrice().add(addOnTotalCost);
    }

    @Override
    public ProductDefinition getProductDefinition() {
        return decoratedProduct.getProductDefinition();
    }

    @Override
    public List<AppliedAddOn> getAppliedAddOns() {
        List<AppliedAddOn> aadOns = new ArrayList<>(decoratedProduct.getAppliedAddOns());
        aadOns.add(new AppliedAddOn(this.addOn, this.quantity));
        return aadOns;
    }
}