package com.fiap.snackapp.adapters.driven.infra.persistence.repository;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataProductJpaRepository extends JpaRepository<ProductEntity, Long>,
                                                        JpaSpecificationExecutor<ProductEntity> {
}