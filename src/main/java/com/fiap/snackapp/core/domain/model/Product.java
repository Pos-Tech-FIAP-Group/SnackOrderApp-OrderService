package com.fiap.snackapp.core.domain.model;

import java.math.BigDecimal;
import java.util.List;

public interface Product {
    record AppliedAddOn(AddOnDefinition addOn, int quantity) {}

    Long getId();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    ProductDefinition getProductDefinition();
    List<AppliedAddOn> getAppliedAddOns();
}
