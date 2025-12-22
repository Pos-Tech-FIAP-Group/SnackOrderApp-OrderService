package com.fiap.snackapp.core.application.repository;

import com.fiap.snackapp.core.application.dto.request.ProductUpdateRequest;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    ProductDefinition save(ProductDefinition productDefinition);
    Optional<ProductDefinition> findById(Long id);
    List<ProductDefinition> findByFilters(Boolean active, Category category);
    ProductDefinition update(Long id, ProductUpdateRequest active);
}