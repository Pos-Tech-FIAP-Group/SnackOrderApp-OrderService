package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.OrderEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.OrderPersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataOrderJpaRepository;
import com.fiap.snackapp.adapters.driven.infra.persistence.specification.OrderSpecifications;
import com.fiap.snackapp.core.application.repository.OrderRepositoryPort;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public OrderDefinition save(OrderDefinition order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OrderDefinition> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<OrderDefinition> findByFilters(List<OrderStatus> orderStatus) {
        Specification<OrderEntity> spec = OrderSpecifications.byFilters(orderStatus);

        return jpaRepository.findAll(spec)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
