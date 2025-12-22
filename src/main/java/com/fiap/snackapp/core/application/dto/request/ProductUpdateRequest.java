package com.fiap.snackapp.core.application.dto.request;

import com.fiap.snackapp.core.domain.enums.Category;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        String name,
        Category category,
        BigDecimal price,
        @Size(max = 255, message = "A descrição não pode exceder 255 caracteres")
        String description,
        Boolean active
) {}
