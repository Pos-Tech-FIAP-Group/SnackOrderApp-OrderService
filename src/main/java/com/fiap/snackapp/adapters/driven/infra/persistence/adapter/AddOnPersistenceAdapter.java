package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.AddOnEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.PersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataAddOnJpaRepository;
import com.fiap.snackapp.adapters.driven.infra.persistence.specification.AddOnSpecifications;
import com.fiap.snackapp.core.application.dto.request.AddOnUpdateRequest;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import com.fiap.snackapp.core.domain.enums.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddOnPersistenceAdapter implements AddOnRepositoryPort {

    private final SpringDataAddOnJpaRepository jpaRepository;
    private final PersistenceMapper mapper;

    @Override
    public AddOnDefinition save(AddOnDefinition addOnDefinition) {
        var entity = mapper.toEntity(addOnDefinition);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AddOnDefinition> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AddOnDefinition> findByFilters(Boolean active, Category category) {
        Specification<AddOnEntity> spec = AddOnSpecifications.byFilters(active, category);

        return jpaRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public AddOnDefinition update(Long id, AddOnUpdateRequest addOnUpdateRequest) {
        AddOnEntity existingAddOn = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adicional não encontrado com id: " + id));

        AddOnEntity updatedAddon = new AddOnEntity(
                existingAddOn.getId(),
                addOnUpdateRequest.name() != null ? addOnUpdateRequest.name() : existingAddOn.getName(),
                addOnUpdateRequest.category() != null ? addOnUpdateRequest.category() : existingAddOn.getCategory(),
                addOnUpdateRequest.price() != null ? addOnUpdateRequest.price() : existingAddOn.getPrice(),
                addOnUpdateRequest.active() != null ? addOnUpdateRequest.active() : existingAddOn.isActive()
        );

        return mapper.toDomain(jpaRepository.save(updatedAddon));
    }
}