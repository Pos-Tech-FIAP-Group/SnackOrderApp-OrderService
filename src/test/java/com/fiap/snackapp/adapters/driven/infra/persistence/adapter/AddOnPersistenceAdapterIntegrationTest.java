package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.AddOnEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.PersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataAddOnJpaRepository;
import com.fiap.snackapp.core.application.dto.request.AddOnUpdateRequest;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PersistenceMapper.class)
class AddOnPersistenceAdapterIntegrationTest {

    @Autowired
    private SpringDataAddOnJpaRepository jpaRepository;

    @Autowired
    private PersistenceMapper mapper;

    @Autowired
    private EntityManager entityManager;

    private AddOnRepositoryPort addOnRepositoryPort;

    private Long baconId;
    private Long queijoId;
    private Long cebolaId;
    private Long refrigeranteId;

    @BeforeEach
    void setUp() {
        addOnRepositoryPort = new AddOnPersistenceAdapter(jpaRepository, mapper);

        jpaRepository.deleteAllInBatch();
        jpaRepository.flush();
        entityManager.clear();

        var bacon = new AddOnEntity(null, "Bacon", Category.LANCHE, BigDecimal.valueOf(2.50), true);
        var queijo = new AddOnEntity(null, "Queijo", Category.LANCHE, BigDecimal.valueOf(3.00), true);
        var cebola = new AddOnEntity(null, "Cebola", Category.LANCHE, BigDecimal.valueOf(1.00), false);
        var refrigerante = new AddOnEntity(null, "Refrigerante", Category.BEBIDA, BigDecimal.valueOf(5.00), true);

        var saved = jpaRepository.saveAllAndFlush(List.of(bacon, queijo, cebola, refrigerante));

        baconId = saved.get(0).getId();
        queijoId = saved.get(1).getId();
        cebolaId = saved.get(2).getId();
        refrigeranteId = saved.get(3).getId();
    }

    @Nested
    @DisplayName("save")
    class SaveTests {
        @Test
        @DisplayName("deve persistir adicional novo ativo")
        void shouldPersistNewAddOn() {
            var newAddOn = new AddOnDefinition(null, "Queijo Extra", Category.LANCHE, BigDecimal.valueOf(3.50), true);

            var saved = addOnRepositoryPort.save(newAddOn);

            assertThat(saved.id()).isNotNull();
            assertThat(saved.name()).isEqualTo("Queijo Extra");
            assertThat(saved.category()).isEqualTo(Category.LANCHE);
            assertThat(saved.price()).isEqualByComparingTo("3.50");
            assertThat(saved.active()).isTrue();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {
        @Test
        @DisplayName("deve retornar adicional ativo por ID")
        void shouldReturnActiveAddOn() {
            var result = addOnRepositoryPort.findById(baconId);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Bacon");
            assertThat(result.get().id()).isEqualTo(baconId);
            assertThat(result.get().active()).isTrue();
        }

        @Test
        @DisplayName("deve retornar adicional inativo por ID")
        void shouldReturnInactiveAddOn() {
            var result = addOnRepositoryPort.findById(cebolaId);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Cebola");
            assertThat(result.get().active()).isFalse();
        }

        @Test
        @DisplayName("deve retornar vazio quando não existe")
        void shouldReturnEmptyWhenNotFound() {
            assertThat(addOnRepositoryPort.findById(999999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFilters")
    class FindByFiltersTests {

        @Test
        @DisplayName("filtro: active=true, category=LANCHE")
        void shouldFilterByActiveTrueAndCategoryLanche() {
            var result = addOnRepositoryPort.findByFilters(true, Category.LANCHE);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(a -> a.active() && a.category() == Category.LANCHE);
        }

        @Test
        @DisplayName("filtro: active=true, category=BEBIDA")
        void shouldFilterByActiveTrueAndCategoryBebida() {
            var result = addOnRepositoryPort.findByFilters(true, Category.BEBIDA);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Refrigerante");
        }

        @Test
        @DisplayName("filtro: active=false, category=LANCHE")
        void shouldFilterByActiveFalseAndCategoryLanche() {
            var result = addOnRepositoryPort.findByFilters(false, Category.LANCHE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Cebola");
            assertThat(result.get(0).active()).isFalse();
        }

        @Test
        @DisplayName("filtro: active=true, category=null (todas categorias ativas)")
        void shouldFilterByActiveTrueAndCategoryNull() {
            var result = addOnRepositoryPort.findByFilters(true, null);

            assertThat(result).hasSize(3);
            assertThat(result).allMatch(AddOnDefinition::active);
        }

        @Test
        @DisplayName("filtro: active=false, category=null (todas categorias inativas)")
        void shouldFilterByActiveFalseAndCategoryNull() {
            var result = addOnRepositoryPort.findByFilters(false, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Cebola");
        }

        @Test
        @DisplayName("filtro: active=null, category=LANCHE (retorna todos da categoria)")
        void shouldFilterByActiveNullAndCategoryLanche() {
            var result = addOnRepositoryPort.findByFilters(null, Category.LANCHE);

            assertThat(result).hasSize(3); // Bacon, Queijo (ativos) + Cebola (inativo)
            assertThat(result).extracting(AddOnDefinition::name)
                    .containsExactlyInAnyOrder("Bacon", "Queijo", "Cebola");
        }

        @Test
        @DisplayName("filtro: active=null, category=BEBIDA (retorna todos da categoria)")
        void shouldFilterByActiveNullAndCategoryBebida() {
            var result = addOnRepositoryPort.findByFilters(null, Category.BEBIDA);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Refrigerante");
        }

        @Test
        @DisplayName("filtro: active=null, category=null (retorna todos)")
        void shouldFilterByActiveNullAndCategoryNull() {
            var result = addOnRepositoryPort.findByFilters(null, null);

            assertThat(result).hasSize(4); // Bacon, Queijo, Cebola, Refrigerante
            assertThat(result).extracting(AddOnDefinition::name)
                    .containsExactlyInAnyOrder("Bacon", "Queijo", "Cebola", "Refrigerante");
        }

        @Test
        @DisplayName("filtro: nenhum resultado")
        void shouldReturnEmptyWhenNoMatch() {
            var result = addOnRepositoryPort.findByFilters(true, Category.SOBREMESA);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("deve atualizar nome")
        void shouldUpdateName() {
            var req = new AddOnUpdateRequest("Bacon Premium", null, null, null);

            var updated = addOnRepositoryPort.update(baconId, req);

            assertThat(updated.id()).isEqualTo(baconId);
            assertThat(updated.name()).isEqualTo("Bacon Premium");
            assertThat(updated.category()).isEqualTo(Category.LANCHE);
            assertThat(updated.price()).isEqualByComparingTo("2.50");
            assertThat(updated.active()).isTrue();
        }

        @Test
        @DisplayName("deve atualizar preço")
        void shouldUpdatePrice() {
            var req = new AddOnUpdateRequest(null, null, BigDecimal.valueOf(4.00), null);

            var updated = addOnRepositoryPort.update(baconId, req);

            assertThat(updated.price()).isEqualByComparingTo("4.00");
            assertThat(updated.name()).isEqualTo("Bacon");
        }

        @Test
        @DisplayName("deve atualizar categoria")
        void shouldUpdateCategory() {
            var req = new AddOnUpdateRequest(null, Category.SOBREMESA, null, null);

            var updated = addOnRepositoryPort.update(baconId, req);

            assertThat(updated.category()).isEqualTo(Category.SOBREMESA);
            assertThat(updated.name()).isEqualTo("Bacon");
        }

        @Test
        @DisplayName("deve atualizar status ativo")
        void shouldUpdateActiveStatus() {
            var req = new AddOnUpdateRequest(null, null, null, false);

            var updated = addOnRepositoryPort.update(baconId, req);

            assertThat(updated.active()).isFalse();
            assertThat(updated.name()).isEqualTo("Bacon");
        }

        @Test
        @DisplayName("deve atualizar todos os campos")
        void shouldUpdateAllFields() {
            var req = new AddOnUpdateRequest("Bacon Especial", Category.BEBIDA, BigDecimal.valueOf(5.50), false);

            var updated = addOnRepositoryPort.update(baconId, req);

            assertThat(updated.id()).isEqualTo(baconId);
            assertThat(updated.name()).isEqualTo("Bacon Especial");
            assertThat(updated.category()).isEqualTo(Category.BEBIDA);
            assertThat(updated.price()).isEqualByComparingTo("5.50");
            assertThat(updated.active()).isFalse();
        }

        @Test
        @DisplayName("deve atualizar parcial (nome e preço)")
        void shouldUpdatePartial() {
            var req = new AddOnUpdateRequest("Bacon Premium", null, BigDecimal.valueOf(4.00), null);

            var updated = addOnRepositoryPort.update(baconId, req);

            assertThat(updated.id()).isEqualTo(baconId);
            assertThat(updated.name()).isEqualTo("Bacon Premium");
            assertThat(updated.price()).isEqualByComparingTo("4.00");
            assertThat(updated.category()).isEqualTo(Category.LANCHE);
            assertThat(updated.active()).isTrue();
        }

        @Test
        @DisplayName("deve lançar não encontrado para id inexistente")
        void shouldThrowNotFoundForNonExistingId() {
            var req = new AddOnUpdateRequest("X", null, null, null);

            assertThatThrownBy(() -> addOnRepositoryPort.update(999999L, req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Adicional não encontrado");
        }

        @Test
        @DisplayName("deve preservar valores não atualizados")
        void shouldPreserveUntouchedFields() {
            var req = new AddOnUpdateRequest(null, null, BigDecimal.valueOf(3.00), null);

            var updated = addOnRepositoryPort.update(cebolaId, req);

            assertThat(updated.name()).isEqualTo("Cebola");
            assertThat(updated.category()).isEqualTo(Category.LANCHE);
            assertThat(updated.active()).isFalse();
            assertThat(updated.price()).isEqualByComparingTo("3.00");
        }
    }
}
