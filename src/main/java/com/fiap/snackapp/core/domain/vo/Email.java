package com.fiap.snackapp.core.domain.vo;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (value != null && !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email inválido");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}