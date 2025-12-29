package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.*;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.mapper.OrderItemMapper;
import com.fiap.snackapp.core.application.mapper.OrderMapper;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.application.repository.OrderRepositoryPort;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.*;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private CustomerRepositoryPort customerRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private AddOnRepositoryPort addOnRepository;

    @InjectMocks
    private OrderUseCaseImpl useCase;

    @Nested
    @DisplayName("Cenários de Início de Pedido")
    class StartOrderTests {

        @Test
        @DisplayName("Deve iniciar pedido para cliente novo (salva cliente)")
        void shouldStartOrderForNewCustomer() {
            var cpfString = "12345678900";

            var savedCustomer =
                    new CustomerDefinition(1L, "Cliente", new Email("default@email.com"), new CPF(cpfString));

            var orderToSave = new OrderDefinition(null, savedCustomer, OrderStatus.INICIADO, new ArrayList<>());
            var savedOrder = new OrderDefinition(10L, savedCustomer, OrderStatus.INICIADO, new ArrayList<>());

            var expectedResponse =
                    new OrderResponse(10L, OrderStatus.INICIADO.name(), cpfString, List.of(), BigDecimal.ZERO);

            // findByCpf uses a NEW CPF(cpfString) inside the use case
            when(customerRepository.findByCpf(any(CPF.class))).thenReturn(Optional.empty());

            // save uses a NEW CustomerDefinition(null,"Cliente",Email("default..."),cpf)
            when(customerRepository.save(argThat(c ->
                    c != null
                            && "Cliente".equals(c.name())
                            && new Email("default@email.com").equals(c.email())
                            && new CPF(cpfString).equals(c.cpf())
            ))).thenReturn(savedCustomer);

            when(orderMapper.toOrderDomain(savedCustomer)).thenReturn(orderToSave);
            when(orderRepository.save(any(OrderDefinition.class))).thenReturn(savedOrder);
            when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

            var response = useCase.initOrder(cpfString);

            assertThat(response).isEqualTo(expectedResponse);

            verify(customerRepository).save(argThat(c ->
                    "Cliente".equals(c.name())
                            && new Email("default@email.com").equals(c.email())
                            && new CPF(cpfString).equals(c.cpf())
            ));
            verify(orderRepository).save(any());
        }

        @Test
        @DisplayName("Deve iniciar pedido para cliente existente (não salva cliente)")
        void shouldStartOrderForExistingCustomer() {
            var cpfString = "12345678900";
            var cpfVo = new CPF(cpfString);
            var existingCustomer = new CustomerDefinition(1L, "João", new Email("joao@email.com"), cpfVo);

            var orderToSave = new OrderDefinition(null, existingCustomer, OrderStatus.INICIADO, new ArrayList<>());
            var savedOrder = new OrderDefinition(10L, existingCustomer, OrderStatus.INICIADO, new ArrayList<>());
            var expectedResponse = new OrderResponse(10L, OrderStatus.INICIADO.name(), cpfString, List.of(), BigDecimal.ZERO);

            when(customerRepository.findByCpf(any(CPF.class))).thenReturn(Optional.of(existingCustomer));
            when(orderMapper.toOrderDomain(existingCustomer)).thenReturn(orderToSave);
            when(orderRepository.save(any(OrderDefinition.class))).thenReturn(savedOrder);
            when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

            var response = useCase.initOrder(cpfString);

            assertThat(response).isEqualTo(expectedResponse);

            verify(customerRepository, never()).save(any());
            verify(orderRepository).save(any());
        }

    }

    @Nested
    @DisplayName("Cenários de Adição de Itens")
    class AddItemsTests {

        @Test
        @DisplayName("Deve adicionar itens com adicionais ao pedido")
        void shouldAddItemsWithAddOns() {
            Long orderId = 1L;
            Long prodId = 10L;
            Long addOnId = 50L;

            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, new ArrayList<>());
            var product = new ProductDefinition(prodId, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            var addOn = new AddOnDefinition(addOnId, "Bacon", Category.LANCHE, BigDecimal.TWO, true);

            var addOnRequest = new AddOnRequest(addOnId, 2);
            var itemRequest = new ItemRequest(prodId, 1, List.of(addOnRequest));
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(productRepository.findById(prodId)).thenReturn(Optional.of(product));
            when(addOnRepository.findById(addOnId)).thenReturn(Optional.of(addOn));

            // CORREÇÃO: Construtor do record OrderItemDefinition
            var itemDomain = new OrderItemDefinition(
                    prodId, "Lanche", 1, BigDecimal.valueOf(14), new ArrayList<>()
            );

            when(orderItemMapper.toDomain(eq(product), eq(itemRequest), anyList())).thenReturn(itemDomain);

            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(mock(OrderResponse.class));

            useCase.addItems(orderId, request);

            assertThat(order.getItems()).hasSize(1);
            verify(addOnRepository).findById(addOnId);
        }

        @Test
        @DisplayName("Deve lançar erro se pedido não existir")
        void shouldThrowIfOrderNotFound() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());
            var req = new OrderItemsRequest(List.of());

            assertThatThrownBy(() -> useCase.addItems(99L, req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve lançar erro se produto não existir")
        void shouldThrowIfProductNotFound() {
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>());
            var itemRequest = new ItemRequest(999L, 1, null);
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.addItems(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Produto não encontrado");
        }
    }

    @Nested
    @DisplayName("Cenários de Atualização de Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Deve atualizar status com transição válida")
        void shouldUpdateStatusSuccessfully() {
            var items = new ArrayList<OrderItemDefinition>();

            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));

            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, items);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            useCase.updateOrderStatus(1L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve falhar ao tentar mudar status de pedido sem itens")
        void shouldFailIfOrderIsEmpty() {
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>());
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem itens");
        }

        @Test
        @DisplayName("Deve falhar com transição de status inválida")
        void shouldFailInvalidTransition() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));

            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, items);
            var request = new OrderStatusUpdateRequest(OrderStatus.CONCLUIDO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição de status inválida");
        }
    }
}
