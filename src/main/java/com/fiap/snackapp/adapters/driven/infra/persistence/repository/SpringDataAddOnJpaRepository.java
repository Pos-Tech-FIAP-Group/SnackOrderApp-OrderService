package com.fiap.snackapp.adapters.driven.infra.persistence.repository;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.AddOnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataAddOnJpaRepository extends JpaRepository<AddOnEntity, Long>,
                                                      JpaSpecificationExecutor<AddOnEntity> {
}