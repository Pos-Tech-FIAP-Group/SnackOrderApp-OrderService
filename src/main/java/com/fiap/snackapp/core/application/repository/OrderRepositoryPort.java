package com.fiap.snackapp.core.application.repository;

import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.OrderDefinition;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    OrderDefinition save(OrderDefinition order);
    Optional<OrderDefinition> findById(Long id);
    List<OrderDefinition> findByFilters(List<OrderStatus> orderStatus);
}