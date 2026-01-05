package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.ProductEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.PersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataProductJpaRepository;
import com.fiap.snackapp.core.application.dto.request.ProductUpdateRequest;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
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
class ProductPersistenceAdapterIntegrationTest {

    @Autowired
    private SpringDataProductJpaRepository jpaRepository;

    @Autowired
    private PersistenceMapper mapper;

    @Autowired
    private EntityManager entityManager;

    private ProductRepositoryPort productRepositoryPort;

    private Long lancheIdId;
    private Long bebidaId;
    private Long sobremesaId;

    @BeforeEach
    void setUp() {
        productRepositoryPort = new ProductPersistenceAdapter(jpaRepository, mapper);

        jpaRepository.deleteAllInBatch();
        jpaRepository.flush();
        entityManager.clear();

        var p1 = new ProductEntity();
        p1.setName("X-Burger");
        p1.setCategory(Category.LANCHE);
        p1.setPrice(BigDecimal.valueOf(25.00));
        p1.setDescription("Hamburguer com queijo");
        p1.setActive(true);

        var p2 = new ProductEntity();
        p2.setName("Coca-Cola");
        p2.setCategory(Category.BEBIDA);
        p2.setPrice(BigDecimal.valueOf(5.00));
        p2.setDescription("Refrigerante gelado");
        p2.setActive(true);

        var p3 = new ProductEntity();
        p3.setName("Pudim");
        p3.setCategory(Category.SOBREMESA);
        p3.setPrice(BigDecimal.valueOf(8.00));
        p3.setDescription("Pudim de leite condensado");
        p3.setActive(false);

        var saved = jpaRepository.saveAllAndFlush(List.of(p1, p2, p3));

        lancheIdId = saved.get(0).getId();
        bebidaId = saved.get(1).getId();
        sobremesaId = saved.get(2).getId();
    }

    @Test
    @DisplayName("save: deve persistir novo produto e retornar com id")
    void save_ShouldPersistNewProduct() {
        var domain = new ProductDefinition(null, "Pizza", Category.LANCHE, BigDecimal.valueOf(35.00), "Pizza grande", true);

        var saved = productRepositoryPort.save(domain);

        assertThat(saved).isNotNull();
        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Pizza");
        assertThat(saved.category()).isEqualTo(Category.LANCHE);
        assertThat(saved.price()).isEqualByComparingTo(BigDecimal.valueOf(35.00));
        assertThat(saved.active()).isTrue();
    }

    @Test
    @DisplayName("findById: deve retornar produto existente")
    void findById_ShouldReturnExistingProduct() {
        var result = productRepositoryPort.findById(lancheIdId);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(lancheIdId);
        assertThat(result.get().name()).isEqualTo("X-Burger");
        assertThat(result.get().category()).isEqualTo(Category.LANCHE);
    }

    @Test
    @DisplayName("findById: deve retornar vazio para id inexistente")
    void findById_ShouldReturnEmpty() {
        var result = productRepositoryPort.findById(999999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFilters: deve filtrar por categoria ativa")
    void findByFilters_ShouldFilterByActiveCategoryLanche() {
        var result = productRepositoryPort.findByFilters(true, Category.LANCHE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(lancheIdId);
        assertThat(result.get(0).active()).isTrue();
    }

    @Test
    @DisplayName("findByFilters: deve filtrar por categoria sem importar status")
    void findByFilters_ShouldFilterByCategory() {
        var result = productRepositoryPort.findByFilters(null, Category.BEBIDA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(bebidaId);
    }

    @Test
    @DisplayName("findByFilters: deve retornar apenas inativos se active=false")
    void findByFilters_ShouldFilterByInactive() {
        var result = productRepositoryPort.findByFilters(false, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(sobremesaId);
        assertThat(result.get(0).active()).isFalse();
    }

    @Test
    @DisplayName("findByFilters: null para ambos deve retornar todos")
    void findByFilters_NullBothShouldReturnAll() {
        var result = productRepositoryPort.findByFilters(null, null);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("update: deve atualizar produto com todos os campos")
    void update_ShouldUpdateAllFields() {
        var updateRequest = new ProductUpdateRequest(
                "X-Burger Premium",
                Category.LANCHE,
                BigDecimal.valueOf(30.00),
                "Hamburguer premium com queijo",
                false
        );

        var updated = productRepositoryPort.update(lancheIdId, updateRequest);

        assertThat(updated.name()).isEqualTo("X-Burger Premium");
        assertThat(updated.price()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
        assertThat(updated.description()).isEqualTo("Hamburguer premium com queijo");
        assertThat(updated.active()).isFalse();
    }

    @Test
    @DisplayName("update: deve atualizar apenas campos não nulos (manter valores antigos)")
    void update_ShouldUpdateOnlyNonNullFields() {
        var updateRequest = new ProductUpdateRequest(
                null, // Mantém nome
                null, // Mantém categoria
                BigDecimal.valueOf(28.00), // Atualiza preço
                null, // Mantém descrição
                null  // Mantém status ativo
        );

        var updated = productRepositoryPort.update(lancheIdId, updateRequest);

        // Validações: deve manter valores antigos exceto preço
        assertThat(updated.name()).isEqualTo("X-Burger");
        assertThat(updated.category()).isEqualTo(Category.LANCHE);
        assertThat(updated.price()).isEqualByComparingTo(BigDecimal.valueOf(28.00));
        assertThat(updated.description()).isEqualTo("Hamburguer com queijo");
        assertThat(updated.active()).isTrue();
    }

    @Test
    @DisplayName("update: deve lançar exceção se produto não existir")
    void update_ShouldThrowIfProductNotFound() {
        var updateRequest = new ProductUpdateRequest(
                "Novo Nome",
                Category.LANCHE,
                BigDecimal.TEN,
                "Nova Desc",
                true
        );

        assertThatThrownBy(() -> productRepositoryPort.update(999999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }
}
