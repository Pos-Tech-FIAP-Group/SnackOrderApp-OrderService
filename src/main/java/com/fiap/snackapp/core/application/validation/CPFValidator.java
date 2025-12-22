package com.fiap.snackapp.core.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null) return false;

        cpf = cpf.replaceAll("\\D", ""); // Remove pontos e traços

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false; // Ex: 111.111.111-11

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
            int digito1 = 11 - (soma % 11);
            if (digito1 >= 10) digito1 = 0;
            if (digito1 != (cpf.charAt(9) - '0')) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
            int digito2 = 11 - (soma % 11);
            if (digito2 >= 10) digito2 = 0;
            return digito2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
