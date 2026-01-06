package com.fiap.snackapp.core.domain.model;

import com.fiap.snackapp.core.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDefinitionTest {

    @Mock
    private CustomerDefinition customer;

    @Mock
    private OrderItemDefinition item1;

    @Mock
    private OrderItemDefinition item2;

    @Test
    @DisplayName("Deve calcular o preço total somando todos os itens")
    void shouldCalculateTotalPriceCorrectly() {
        // Cenário: Item 1 custa 10.00, Item 2 custa 5.50
        when(item1.getTotalPrice()).thenReturn(new BigDecimal("10.00"));
        when(item2.getTotalPrice()).thenReturn(new BigDecimal("5.50"));

        List<OrderItemDefinition> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        // Usando o Enum correto: INICIADO
        var order = new OrderDefinition(1L, customer, OrderStatus.INICIADO, items);

        // Validação: 10 + 5.50 = 15.50
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("15.50"));
    }

    @Test
    @DisplayName("Deve retornar zero se o pedido não tiver itens")
    void shouldReturnZeroTotalPriceWhenNoItems() {
        var order = new OrderDefinition(1L, customer, OrderStatus.INICIADO, null);

        assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve inicializar lista vazia se passar null no construtor")
    void shouldInitializeEmptyListWhenNullPassed() {
        var order = new OrderDefinition(1L, customer, OrderStatus.INICIADO, null);

        assertThat(order.getItems()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Deve adicionar itens à lista")
    void shouldAddItemToList() {
        var order = new OrderDefinition(1L, customer, OrderStatus.INICIADO, new ArrayList<>());

        order.addItem(item1);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0)).isEqualTo(item1);
    }

    @Test
    @DisplayName("Deve cobrir Getters e Setters gerados pelo Lombok")
    void shouldCoverLombokGettersAndSetters() {
        // Cria com valores iniciais
        var list = new ArrayList<OrderItemDefinition>();
        var order = new OrderDefinition(10L, customer, OrderStatus.INICIADO, list);

        // Teste Getters (Lombok)
        assertThat(order.getId()).isEqualTo(10L);
        assertThat(order.getCustomer()).isEqualTo(customer);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.INICIADO);
        assertThat(order.getItems()).isEqualTo(list);

        // Teste Setter (Lombok - Apenas Status é mutável)
        // Mudando de INICIADO para PAGAMENTO_PENDENTE
        order.setStatus(OrderStatus.PAGAMENTO_PENDENTE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);
    }
}
