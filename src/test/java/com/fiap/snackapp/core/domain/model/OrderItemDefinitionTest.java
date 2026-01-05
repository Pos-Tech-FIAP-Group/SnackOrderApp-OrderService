package com.fiap.snackapp.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemDefinitionTest {

    @Mock
    private AppliedAddOn appliedAddOn1;

    @Mock
    private AppliedAddOn appliedAddOn2;

    @Mock
    private AddOnDefinition addOnDefinition1;

    @Mock
    private AddOnDefinition addOnDefinition2;

    @Test
    @DisplayName("Deve calcular preço total com adicionais e quantidade do item")
    void shouldCalculateTotalPriceWithAddOnsAndQuantity() {
        // Cenário:
        // Produto custa 20.00
        // Adicional 1 (Bacon): 2.00 * 2 unidades = 4.00
        // Adicional 2 (Queijo): 1.50 * 1 unidade = 1.50
        // Preço Unitário Combinado: 20 + 4 + 1.50 = 25.50
        // Quantidade do Item no Pedido: 2 lanches
        // Total Esperado: 25.50 * 2 = 51.00

        // Configuração dos Mocks
        when(appliedAddOn1.getAddOnDefinition()).thenReturn(addOnDefinition1);
        when(appliedAddOn1.getQuantity()).thenReturn(2);
        when(addOnDefinition1.price()).thenReturn(new BigDecimal("2.00"));

        when(appliedAddOn2.getAddOnDefinition()).thenReturn(addOnDefinition2);
        when(appliedAddOn2.getQuantity()).thenReturn(1);
        when(addOnDefinition2.price()).thenReturn(new BigDecimal("1.50"));

        List<AppliedAddOn> addOns = List.of(appliedAddOn1, appliedAddOn2);

        var orderItem = new OrderItemDefinition(
                1L,
                "X-Tudo",
                2, // Quantidade do item
                new BigDecimal("20.00"), // Preço base
                addOns
        );

        assertThat(orderItem.getTotalPrice())
                .isEqualByComparingTo(new BigDecimal("51.00"));
    }

    @Test
    @DisplayName("Deve calcular preço total sem adicionais")
    void shouldCalculateTotalPriceWithoutAddOns() {
        // Cenário: 2 Coca-Colas a 5.00 cada = 10.00
        var orderItem = new OrderItemDefinition(
                2L,
                "Coca-Cola",
                2,
                new BigDecimal("5.00"),
                Collections.emptyList()
        );

        assertThat(orderItem.getTotalPrice())
                .isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Deve testar métodos gerados pelo Record (Getters, Equals, HashCode)")
    void shouldCoverRecordGeneratedMethods() {
        List<AppliedAddOn> emptyList = Collections.emptyList();

        var item1 = new OrderItemDefinition(1L, "Lanche", 1, BigDecimal.TEN, emptyList);
        var item2 = new OrderItemDefinition(1L, "Lanche", 1, BigDecimal.TEN, emptyList);
        var item3 = new OrderItemDefinition(2L, "Suco", 1, BigDecimal.ONE, emptyList);

        // Accessors (Getters nativos do Record)
        assertThat(item1.productId()).isEqualTo(1L);
        assertThat(item1.productName()).isEqualTo("Lanche");
        assertThat(item1.quantity()).isEqualTo(1);
        assertThat(item1.price()).isEqualTo(BigDecimal.TEN);
        assertThat(item1.appliedAddOns()).isEqualTo(emptyList);

        // Equals & HashCode
        assertThat(item1)
                .isEqualTo(item2)
                .hasSameHashCodeAs(item2)
                .isNotEqualTo(item3)
                .isNotEqualTo(null);

        // ToString
        assertThat(item1.toString())
                .contains("OrderItemDefinition")
                .contains("Lanche");
    }
}
