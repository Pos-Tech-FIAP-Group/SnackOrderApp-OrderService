package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.ProductEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.PersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataProductJpaRepository;
import com.fiap.snackapp.adapters.driven.infra.persistence.specification.ProductSpecifications;
import com.fiap.snackapp.core.application.dto.request.ProductUpdateRequest;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final SpringDataProductJpaRepository jpaRepository;
    private final PersistenceMapper mapper;

    @Override
    public ProductDefinition save(ProductDefinition productDefinition) {
        var entity = mapper.toEntity(productDefinition);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductDefinition> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ProductDefinition> findByFilters(Boolean active, Category category) {
        Specification<ProductEntity> spec = ProductSpecifications.byFilters(active, category);

        return jpaRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public ProductDefinition update(Long id, ProductUpdateRequest productUpdateRequest) {
        ProductEntity existingProduct = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        ProductEntity updatedProduct = new ProductEntity(
                existingProduct.getId(),
                productUpdateRequest.name() != null ? productUpdateRequest.name() : existingProduct.getName(),
                productUpdateRequest.category() != null ? productUpdateRequest.category() : existingProduct.getCategory(),
                productUpdateRequest.price() != null ? productUpdateRequest.price() : existingProduct.getPrice(),
                productUpdateRequest.description() != null ? productUpdateRequest.description() : existingProduct.getDescription(),
                productUpdateRequest.active() != null ? productUpdateRequest.active() : existingProduct.isActive()
        );

        return mapper.toDomain(jpaRepository.save(updatedProduct));
    }
}