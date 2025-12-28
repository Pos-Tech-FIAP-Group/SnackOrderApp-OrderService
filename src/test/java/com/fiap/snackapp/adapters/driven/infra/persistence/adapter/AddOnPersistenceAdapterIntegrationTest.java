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

    @BeforeEach
    void setUp() {
        addOnRepositoryPort = new AddOnPersistenceAdapter(jpaRepository, mapper);

        // limpa (bulk) + sincroniza o estado do EntityManager
        jpaRepository.deleteAllInBatch();
        jpaRepository.flush();
        entityManager.clear();

        // IMPORTANTE: id = null (deixa o @GeneratedValue gerar)
        var bacon = new AddOnEntity(null, "Bacon", Category.LANCHE, BigDecimal.valueOf(2.50), true);
        var queijo = new AddOnEntity(null, "Queijo", Category.LANCHE, BigDecimal.valueOf(3.00), true);
        var cebola = new AddOnEntity(null, "Cebola", Category.LANCHE, BigDecimal.valueOf(1.00), false);

        var saved = jpaRepository.saveAllAndFlush(List.of(bacon, queijo, cebola));

        baconId = saved.get(0).getId();
        queijoId = saved.get(1).getId();
        cebolaId = saved.get(2).getId();
    }

    @Test
    @DisplayName("save: deve persistir adicional novo")
    void save_ShouldPersistNewAddOn() {
        var newAddOn = new AddOnDefinition(null, "Queijo Extra", Category.LANCHE, BigDecimal.valueOf(3.50), true);

        var saved = addOnRepositoryPort.save(newAddOn);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Queijo Extra");
    }

    @Test
    @DisplayName("findById: deve retornar existente")
    void findById_ShouldReturnExisting() {
        var result = addOnRepositoryPort.findById(baconId);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Bacon");
    }

    @Test
    @DisplayName("findById: deve retornar vazio quando não existe")
    void findById_ShouldReturnEmpty() {
        assertThat(addOnRepositoryPort.findById(999999L)).isEmpty();
    }

    @Test
    @DisplayName("findByFilters: deve filtrar por active e category")
    void findByFilters_ShouldFilter() {
        var result = addOnRepositoryPort.findByFilters(true, Category.LANCHE);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(AddOnDefinition::active);
        assertThat(result).allMatch(a -> a.category() == Category.LANCHE);
    }

    @Test
    @DisplayName("update: deve atualizar parcial")
    void update_ShouldUpdatePartial() {
        var req = new AddOnUpdateRequest("Bacon Premium", null, BigDecimal.valueOf(4.00), null);

        var updated = addOnRepositoryPort.update(baconId, req);

        assertThat(updated.id()).isEqualTo(baconId);
        assertThat(updated.name()).isEqualTo("Bacon Premium");
        assertThat(updated.price()).isEqualByComparingTo("4.00");
        assertThat(updated.category()).isEqualTo(Category.LANCHE);
        assertThat(updated.active()).isTrue();
    }

    @Test
    @DisplayName("update: deve lançar not found")
    void update_ShouldThrowNotFoundForNonExistingId() {
        var req = new AddOnUpdateRequest("X", null, null, null);

        assertThatThrownBy(() -> addOnRepositoryPort.update(999999L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
