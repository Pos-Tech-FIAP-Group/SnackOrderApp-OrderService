package com.fiap.snackapp.core.domain.vo;


import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;

public record CPF(String value) {
    public CPF {
        CPFValidator cpfValidator = new CPFValidator();
        if (value == null || !value.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}