package com.fiap.snackapp.core.domain.vo;

public record CPF(String value) {
    public CPF {
        if (value == null || !value.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}