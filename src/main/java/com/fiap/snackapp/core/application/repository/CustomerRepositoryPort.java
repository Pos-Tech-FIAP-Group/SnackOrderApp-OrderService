package com.fiap.snackapp.core.application.repository;

import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;

import java.util.Optional;


public interface CustomerRepositoryPort {
    CustomerDefinition save(CustomerDefinition user);
    Optional<CustomerDefinition> findByCpf(CPF cpf);
}