package com.fiap.snackapp.adapters.driven.infra.persistence.repository;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataOrderJpaRepository extends JpaRepository<OrderEntity, Long>,
                                                      JpaSpecificationExecutor<OrderEntity> {
}