package com.fiap.snackapp.core.domain.model;

import com.fiap.snackapp.core.domain.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDefinitionTest {

    @Test
    @DisplayName("Deve criar produto usando construtor auxiliar (sem ID, ativo por padrão)")
    void shouldCreateProductUsingAuxiliaryConstructor() {
        String name = "X-Salada";
        Category category = Category.LANCHE;
        BigDecimal price = new BigDecimal("25.00");
        String description = "Delicioso";

        // Usa o construtor curto: new ProductDefinition(name, category, price, description)
        var product = new ProductDefinition(name, category, price, description);

        // Validações
        assertThat(product.id()).isNull(); // ID deve ser nulo
        assertThat(product.active()).isTrue(); // Deve ser ativo por padrão

        // Valida campos
        assertThat(product.name()).isEqualTo(name);
        assertThat(product.category()).isEqualTo(category);
        assertThat(product.price()).isEqualTo(price);
        assertThat(product.description()).isEqualTo(description);
    }

    @Test
    @DisplayName("Deve implementar corretamente os métodos da interface Product")
    void shouldImplementProductInterfaceMethods() {
        Long id = 1L;
        String name = "Coca-Cola";
        Category category = Category.BEBIDA;
        BigDecimal price = new BigDecimal("5.00");
        String description = "Gelada";
        boolean active = false;

        // Usa construtor canônico (completo)
        var product = new ProductDefinition(id, name, category, price, description, active);

        // Teste getId() da interface vs id() do record
        assertThat(product.getId()).isEqualTo(id);

        // Teste getName() da interface vs name() do record
        assertThat(product.getName()).isEqualTo(name);

        // Teste getDescription() da interface vs description() do record
        assertThat(product.getDescription()).isEqualTo(description);

        // Teste getPrice() da interface vs price() do record
        assertThat(product.getPrice()).isEqualTo(price);

        // Teste getProductDefinition() deve retornar o próprio objeto
        assertThat(product.getProductDefinition()).isSameAs(product);

        // Teste getAppliedAddOns() deve retornar lista vazia imutável
        List<Product.AppliedAddOn> addOns = product.getAppliedAddOns();
        assertThat(addOns).isEmpty();
    }

    @Test
    @DisplayName("Deve testar métodos gerados pelo Record (equals, hashCode, toString)")
    void shouldCoverRecordGeneratedMethods() {
        var prod1 = new ProductDefinition(1L, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true);
        var prod2 = new ProductDefinition(1L, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true); // Igual ao 1
        var prod3 = new ProductDefinition(2L, "Outro", Category.LANCHE, BigDecimal.ONE, "Outro", false); // Diferente

        // Equals & HashCode
        assertThat(prod1)
                .isEqualTo(prod2)
                .hasSameHashCodeAs(prod2)
                .isNotEqualTo(prod3)
                .isNotNull()
                .isNotEqualTo(new Object());

        // ToString
        assertThat(prod1.toString())
                .contains("ProductDefinition")
                .contains("Lanche")
                .contains("active=true");

        // Accessors nativos do record (para garantir cobertura total)
        assertThat(prod1.category()).isEqualTo(Category.LANCHE);
        assertThat(prod1.active()).isTrue();
    }
}
