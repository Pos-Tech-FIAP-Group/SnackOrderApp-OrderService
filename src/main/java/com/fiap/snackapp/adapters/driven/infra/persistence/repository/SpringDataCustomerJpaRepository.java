package com.fiap.snackapp.adapters.driven.infra.persistence.repository;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataCustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByCpf(String cpf);
}