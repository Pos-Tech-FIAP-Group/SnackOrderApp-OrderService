package com.fiap.snackapp.adapters.driven.infra.persistence.mapper;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.AddOnEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.entity.ProductEntity;
import com.fiap.snackapp.core.application.dto.request.ProductCreateRequest;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import org.springframework.stereotype.Component;

@Component
public class PersistenceMapper {

    public ProductResponse toProductResponse(ProductDefinition product) {
        if (product == null) return null;
        return new ProductResponse(
                product.id(),
                product.name(),
                product.category(),
                product.price(),
                product.description(),
                product.active()
        );
    }

    public ProductDefinition toProductDefinition(ProductCreateRequest dto) {
        if (dto == null) return null;
        return new ProductDefinition(
                dto.name(),
                dto.category(),
                dto.price(),
                dto.description()
        );
    }

    public ProductDefinition toDomain(ProductEntity entity) {
        if (entity == null) return null;
        return new ProductDefinition(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getDescription(),
                entity.isActive()
        );
    }

    public ProductEntity toEntity(ProductDefinition domain) {
        if (domain == null) return null;
        ProductEntity entity = new ProductEntity(domain.name(), domain.category(), domain.getPrice(), domain.name());
        if (domain.id() != null) {
            entity.setId(domain.id());
        }
        return entity;
    }

    public AddOnDefinition toDomain(AddOnEntity entity) {
        if (entity == null) return null;
        return new AddOnDefinition(entity.getId(), entity.getName(), entity.getCategory(), entity.getPrice(), entity.isActive());
    }

    public AddOnEntity toEntity(AddOnDefinition domain) {
        if (domain == null) return null;
        AddOnEntity entity = new AddOnEntity(domain.name(), domain.category(), domain.price());
        if (domain.id() != null) {
            entity.setId(domain.id());
        }
        return entity;
    }
}