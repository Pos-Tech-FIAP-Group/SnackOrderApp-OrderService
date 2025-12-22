package com.fiap.snackapp.core.application.dto.request;

import com.fiap.snackapp.core.domain.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddOnUpdateRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        String name,
        Category category,
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser positivo")
        BigDecimal price,
        Boolean active
) {}
