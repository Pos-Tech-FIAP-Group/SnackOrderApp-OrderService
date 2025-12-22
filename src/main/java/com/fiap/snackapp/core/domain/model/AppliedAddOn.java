package com.fiap.snackapp.core.domain.model;

import lombok.Getter;

@Getter
public class AppliedAddOn {

    private final AddOnDefinition addOnDefinition;
    private final int quantity;

    public AppliedAddOn(AddOnDefinition addOnDefinition, int quantity) {
        this.addOnDefinition = addOnDefinition;
        this.quantity = quantity;
    }

}