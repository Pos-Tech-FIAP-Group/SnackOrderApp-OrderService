package com.fiap.snackapp.core.application.repository;

import com.fiap.snackapp.core.application.dto.request.AddOnUpdateRequest;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import com.fiap.snackapp.core.domain.enums.Category;

import java.util.List;
import java.util.Optional;

public interface AddOnRepositoryPort {
    AddOnDefinition save(AddOnDefinition addOnDefinition);
    Optional<AddOnDefinition> findById(Long id);
    List<AddOnDefinition> findByFilters(Boolean active, Category category);
    AddOnDefinition update(Long id, AddOnUpdateRequest addOnUpdateRequest);
}