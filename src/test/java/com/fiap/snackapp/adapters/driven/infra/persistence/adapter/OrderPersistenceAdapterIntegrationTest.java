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
import org.junit.jupiter.api.Nested;
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
    private Long orderConcluidoId;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = new OrderPersistenceAdapter(jpaRepository, mapper);

        jpaRepository.deleteAllInBatch();
        jpaRepository.flush();
        entityManager.clear();

        // Criação de cenários de teste
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

        // Apenas guardamos os IDs que serão explicitamente usados nos testes
        orderIniciadoId = saved.get(0).getId();
        orderConcluidoId = saved.get(2).getId();
    }

    @Nested
    @DisplayName("save")
    class SaveTests {
        @Test
        @DisplayName("deve persistir pedido novo e retornar com id")
        void shouldPersistNewOrder() {
            var domain = new OrderDefinition(null, null, OrderStatus.INICIADO, new ArrayList<>(),  null, null);

            var saved = orderRepositoryPort.save(domain);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(OrderStatus.INICIADO);
            assertThat(saved.getItems()).isEmpty();
            assertThat(saved.getCustomer()).isNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {
        @Test
        @DisplayName("deve retornar pedido existente (INICIADO)")
        void shouldReturnExistingOrder() {
            var result = orderRepositoryPort.findById(orderIniciadoId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(orderIniciadoId);
            assertThat(result.get().getStatus()).isEqualTo(OrderStatus.INICIADO);
        }

        @Test
        @DisplayName("deve retornar pedido concluído por ID")
        void shouldReturnConcluidoOrder() {
            var result = orderRepositoryPort.findById(orderConcluidoId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(orderConcluidoId);
            assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CONCLUIDO);
        }

        @Test
        @DisplayName("deve retornar vazio para id inexistente")
        void shouldReturnEmpty() {
            assertThat(orderRepositoryPort.findById(999999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFilters")
    class FindByFiltersTests {

        @Test
        @DisplayName("filtro: 1 status (INICIADO)")
        void shouldFilterBySingleStatus() {
            var result = orderRepositoryPort.findByFilters(List.of(OrderStatus.INICIADO));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(orderIniciadoId);
            assertThat(result.getFirst().getStatus()).isEqualTo(OrderStatus.INICIADO);
        }

        @Test
        @DisplayName("filtro: múltiplos status (INICIADO e CONCLUIDO)")
        void shouldFilterByMultipleStatus() {
            var result = orderRepositoryPort.findByFilters(List.of(OrderStatus.INICIADO, OrderStatus.CONCLUIDO));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(OrderDefinition::getStatus)
                    .containsExactlyInAnyOrder(OrderStatus.INICIADO, OrderStatus.CONCLUIDO);
        }

        @Test
        @DisplayName("filtro: todos os status (INICIADO, PENDENTE, CONCLUIDO)")
        void shouldFilterByAllStatus() {
            var result = orderRepositoryPort.findByFilters(
                    List.of(OrderStatus.INICIADO, OrderStatus.PAGAMENTO_PENDENTE, OrderStatus.CONCLUIDO)
            );

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("filtro: null deve retornar todos")
        void shouldReturnAllWhenNullFilter() {
            var result = orderRepositoryPort.findByFilters(null);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("filtro: lista vazia retorna nada")
        void shouldReturnEmptyWhenEmptyFilter() {
            var result = orderRepositoryPort.findByFilters(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filtro: status que não existe retorna vazio")
        void shouldReturnEmptyWhenStatusNotFound() {
            var result = orderRepositoryPort.findByFilters(List.of(OrderStatus.PAGAMENTO_APROVADO));

            assertThat(result).isEmpty();
        }
    }
}
