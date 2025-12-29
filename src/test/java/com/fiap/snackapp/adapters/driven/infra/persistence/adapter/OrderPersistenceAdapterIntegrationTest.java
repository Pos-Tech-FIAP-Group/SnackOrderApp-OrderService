package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.OrderEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.OrderPersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataOrderJpaRepository;
import com.fiap.snackapp.core.application.repository.OrderRepositoryPort;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OrderPersistenceMapper.class)
class OrderPersistenceAdapterIntegrationTest {

    @Autowired
    private SpringDataOrderJpaRepository jpaRepository;

    @Autowired
    private OrderPersistenceMapper mapper;

    @Autowired
    private EntityManager entityManager;

    private OrderRepositoryPort orderRepositoryPort;

    private Long orderIniciadoId;
    private Long orderRecebidoId;
    private Long orderProntoId;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = new OrderPersistenceAdapter(jpaRepository, mapper);

        jpaRepository.deleteAllInBatch();
        jpaRepository.flush();
        entityManager.clear();

        var o1 = new OrderEntity();
        o1.setStatus(OrderStatus.INICIADO);
        o1.setCustomer(null);
        o1.setItems(new HashSet<>());

        var o2 = new OrderEntity();
        o2.setStatus(OrderStatus.PAGAMENTO_PENDENTE);
        o2.setCustomer(null);
        o2.setItems(new HashSet<>());

        var o3 = new OrderEntity();
        o3.setStatus(OrderStatus.CONCLUIDO);
        o3.setCustomer(null);
        o3.setItems(new HashSet<>());

        var saved = jpaRepository.saveAllAndFlush(List.of(o1, o2, o3));

        orderIniciadoId = saved.get(0).getId();
        orderRecebidoId = saved.get(1).getId();
        orderProntoId = saved.get(2).getId();
    }

    @Test
    @DisplayName("save: deve persistir pedido novo e retornar com id")
    void save_ShouldPersistNewOrder() {
        var domain = new OrderDefinition(null, null, OrderStatus.INICIADO, new ArrayList<>());

        var saved = orderRepositoryPort.save(domain);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.INICIADO);
        assertThat(saved.getItems()).isEmpty();
        assertThat(saved.getCustomer()).isNull();
    }

    @Test
    @DisplayName("findById: deve retornar pedido existente")
    void findById_ShouldReturnExistingOrder() {
        var result = orderRepositoryPort.findById(orderRecebidoId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(orderRecebidoId);
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.INICIADO);
    }

    @Test
    @DisplayName("findById: deve retornar vazio para id inexistente")
    void findById_ShouldReturnEmpty() {
        assertThat(orderRepositoryPort.findById(999999L)).isEmpty();
    }

    @Test
    @DisplayName("findByFilters: deve filtrar por 1 status")
    void findByFilters_ShouldFilterBySingleStatus() {
        var result = orderRepositoryPort.findByFilters(List.of(OrderStatus.INICIADO));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(orderIniciadoId);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.INICIADO);
    }

    @Test
    @DisplayName("findByFilters: deve filtrar por lista de status")
    void findByFilters_ShouldFilterByMultipleStatus() {
        var result = orderRepositoryPort.findByFilters(List.of(OrderStatus.INICIADO, OrderStatus.CONCLUIDO));

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(o ->
                o.getStatus() == OrderStatus.INICIADO || o.getStatus() == OrderStatus.CONCLUIDO
        );
    }

    @Test
    @DisplayName("findByFilters: null deve retornar tudo (se o Specification tratar assim)")
    void findByFilters_NullShouldReturnAll() {
        var result = orderRepositoryPort.findByFilters(null);

        assertThat(result).hasSize(3);
    }
}
