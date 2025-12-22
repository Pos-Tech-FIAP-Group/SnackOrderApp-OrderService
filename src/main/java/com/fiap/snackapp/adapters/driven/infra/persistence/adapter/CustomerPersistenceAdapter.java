package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.CustomerEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.CustomerPersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataCustomerJpaRepository;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.domain.vo.CPF;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final SpringDataCustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;

    @Override
    public CustomerDefinition save(CustomerDefinition customerDefinition) {
        CustomerEntity entity = mapper.toEntity(customerDefinition);
        CustomerEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CustomerDefinition> findByCpf(CPF cpf) {
        return jpaRepository.findByCpf(cpf.toString())
                .map(mapper::toDomain);
    }
}
