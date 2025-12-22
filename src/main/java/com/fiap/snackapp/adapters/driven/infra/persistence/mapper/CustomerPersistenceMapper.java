package com.fiap.snackapp.adapters.driven.infra.persistence.mapper;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.CustomerEntity;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceMapper {

    public CustomerEntity toEntity(CustomerDefinition domain) {
        if (domain == null) return null;
        String email = domain.email() != null ? domain.email().toString() : null;
        CustomerEntity entity = new CustomerEntity(domain.name(), email, domain.cpf().toString());
        if (domain.id() != null) {
            entity.setId(domain.id());
        }
        return entity;
    }

    public CustomerDefinition toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        return new CustomerDefinition(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                new CPF(entity.getCpf())
        );
    }
}
