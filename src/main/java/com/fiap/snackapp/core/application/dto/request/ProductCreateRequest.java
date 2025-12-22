package com.fiap.snackapp.core.application.dto.request;

import com.fiap.snackapp.core.domain.enums.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        String name,
        @NotNull(message = "A categoria é obrigatória")
        Category category,
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser positivo")
        BigDecimal price,
        @Size(max = 255, message = "A descrição não pode exceder 255 caracteres")
        String description
) {}
