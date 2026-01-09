package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.*;
import com.fiap.snackapp.core.application.dto.response.OrderPaymentCreatedMessageResponse;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderUseCaseImpl useCase;

    @Nested
    @DisplayName("Cenários de Início de Pedido")
    class StartOrderTests {
        @Test
        @DisplayName("Deve iniciar pedido para cliente novo (salva cliente)")
        void shouldStartOrderForNewCustomer() {
            var cpfString = "12345678900";
            var savedCustomer = new CustomerDefinition(1L, "Cliente", new Email("default@email.com"), new CPF(cpfString));

            var orderToSave = new OrderDefinition(null, savedCustomer, OrderStatus.INICIADO, new ArrayList<>(), null, null);
            var savedOrder = new OrderDefinition(10L, savedCustomer, OrderStatus.INICIADO, new ArrayList<>(), null, null);
            var expectedResponse = new OrderResponse(10L, OrderStatus.INICIADO.name(), cpfString, List.of(), BigDecimal.ZERO, null, null);

            when(customerRepository.findByCpf(any(CPF.class))).thenReturn(Optional.empty());
            when(customerRepository.save(any(CustomerDefinition.class))).thenReturn(savedCustomer);
            when(orderMapper.toOrderDomain(savedCustomer)).thenReturn(orderToSave);
            when(orderRepository.save(any(OrderDefinition.class))).thenReturn(savedOrder);
            when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

            var response = useCase.initOrder(cpfString);

            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).save(any(CustomerDefinition.class));
            verify(orderRepository).save(any(OrderDefinition.class));
        }

        @Test
        @DisplayName("Deve iniciar pedido para cliente existente (não salva cliente)")
        void shouldStartOrderForExistingCustomer() {
            var cpfString = "12345678900";
            var existingCustomer = new CustomerDefinition(1L, "João", new Email("joao@email.com"), new CPF(cpfString));
            var savedOrder = new OrderDefinition(10L, existingCustomer, OrderStatus.INICIADO, new ArrayList<>(), null, null);
            var expectedResponse = new OrderResponse(10L, OrderStatus.INICIADO.name(), cpfString, List.of(), BigDecimal.ZERO, null, null);

            when(customerRepository.findByCpf(any(CPF.class))).thenReturn(Optional.of(existingCustomer));
            when(orderMapper.toOrderDomain(existingCustomer)).thenReturn(new OrderDefinition(null, existingCustomer, OrderStatus.INICIADO, new ArrayList<>(), null, null));
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

            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, new ArrayList<>(), null, null);
            var product = new ProductDefinition(prodId, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            var addOn = new AddOnDefinition(addOnId, "Bacon", Category.LANCHE, BigDecimal.TWO, true);

            var addOnRequest = new AddOnRequest(addOnId, 2);
            var itemRequest = new ItemRequest(prodId, 1, List.of(addOnRequest));
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(productRepository.findById(prodId)).thenReturn(Optional.of(product));
            when(addOnRepository.findById(addOnId)).thenReturn(Optional.of(addOn));
            when(orderItemMapper.toDomain(eq(product), eq(itemRequest), anyList()))
                    .thenReturn(new OrderItemDefinition(prodId, "Lanche", 1, BigDecimal.valueOf(14), new ArrayList<>()));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(mock(OrderResponse.class));

            useCase.addItems(orderId, request);

            assertThat(order.getItems()).hasSize(1);
            verify(addOnRepository).findById(addOnId);
        }

        @Test
        @DisplayName("Deve adicionar itens sem adicionais (lista nula)")
        void shouldAddItemsWhenAddOnsIsNull() {
            Long orderId = 1L;
            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, new ArrayList<>(), null, null);
            var product = new ProductDefinition(10L, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            var itemRequest = new ItemRequest(10L, 1, null);
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(orderItemMapper.toDomain(product, itemRequest, List.of()))
                    .thenReturn(new OrderItemDefinition(10L, "Lanche", 1, BigDecimal.TEN, new ArrayList<>()));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(mock(OrderResponse.class));

            useCase.addItems(orderId, request);

            assertThat(order.getItems()).hasSize(1);
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
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>(), null, null);
            var itemRequest = new ItemRequest(999L, 1, null);
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.addItems(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Produto não encontrado");
        }

        @Test
        @DisplayName("Deve lançar erro se Adicional não existir")
        void shouldThrowIfAddOnNotFound() {
            Long orderId = 1L;
            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, new ArrayList<>(),null, null);
            var product = new ProductDefinition(10L, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            var addOnRequest = new AddOnRequest(999L, 1);
            var itemRequest = new ItemRequest(10L, 1, List.of(addOnRequest));
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(addOnRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.addItems(orderId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Adicional não encontrado");
        }
    }

    @Nested
    @DisplayName("Cenários de RabbitMQ e Integração")
    class RabbitMQTests {

        @Test
        @DisplayName("Deve atualizar status e enviar requisição de pagamento para fila correta")
        void shouldSendPaymentRequestToQueue() {
            // Arrange
            Long orderId = 1L;
            var request = new OrderPaymentCreateRequest(orderId, BigDecimal.TEN, 99L);

            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Item", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, items, null, null);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(OrderDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            useCase.requestOrderPaymentCreation(request);

            // Assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);
            verify(orderRepository).save(order);

            // Verifica o envio para a fila
            verify(rabbitTemplate).convertAndSend(
                    "payment.exchange",
                    "payment.create",
                    request
            );
        }

        @Test
        @DisplayName("Deve enviar pedido para cozinha")
        void shouldSendOrderToKitchen() {
            var order = new OrderDefinition(10L, null, OrderStatus.PAGAMENTO_APROVADO, List.of(), null, null);
            var kitchenRequest = new OrderToKitchenRequest(10L, List.of());

            when(orderMapper.toKitchenRequest(order)).thenReturn(kitchenRequest);

            useCase.sendOrderToKitchen(order);

            verify(rabbitTemplate).convertAndSend("kitchen.order.received", kitchenRequest);
        }

        @Test
        @DisplayName("Deve processar callback de QR Code (atualiza status e dados)")
        void shouldProcessQrCodeCallback() {
            // Arrange
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Item", 1, BigDecimal.TEN, new ArrayList<>()));

            var order = new OrderDefinition(55L, null, OrderStatus.PAGAMENTO_PENDENTE, items, null, null);

            when(orderRepository.findById(55L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(OrderDefinition.class))).thenAnswer(i -> i.getArgument(0));

            var responseMsg = new OrderPaymentCreatedMessageResponse(
                    "pay-uuid", 55L, BigDecimal.TEN, "http://qr.code", OrderStatus.PAGAMENTO_PENDENTE
            );

            // Act
            useCase.updateOrderWithQrCode(responseMsg);

            // Assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);

            assertThat(order.getQrCodeUrl()).isEqualTo("http://qr.code");
            assertThat(order.getPaymentId()).isEqualTo("pay-uuid");

            verify(orderRepository, atLeastOnce()).save(order);
        }

    }

    @Nested
    @DisplayName("Cenários de Atualização de Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Deve atualizar status INICIADO -> PAGAMENTO_PENDENTE")
        void shouldUpdateStatusIniciadoToPendente() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            useCase.updateOrderStatus(1L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);
            verify(orderRepository).save(order);
        }

        // ... (restante dos testes de UpdateStatusTests permanecem inalterados) ...

        @Test
        @DisplayName("Deve atualizar status PAGAMENTO_PENDENTE -> PAGAMENTO_APROVADO")
        void shouldUpdateStatusPendenteToAprovado() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(2L, null, OrderStatus.PAGAMENTO_PENDENTE, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_APROVADO);

            when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            useCase.updateOrderStatus(2L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_APROVADO);
            verify(orderRepository).save(order);
            verifyNoInteractions(rabbitTemplate);
            verify(orderMapper, never()).toKitchenRequest(any());
        }

        @Test
        @DisplayName("Deve atualizar status PAGAMENTO_PENDENTE -> PAGAMENTO_RECUSADO")
        void shouldUpdateStatusPendenteToRecusado() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(3L, null, OrderStatus.PAGAMENTO_PENDENTE, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_RECUSADO);

            when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            useCase.updateOrderStatus(3L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_RECUSADO);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve atualizar status PAGAMENTO_RECUSADO -> CANCELADO")
        void shouldUpdateStatusRecusadoToCancelado() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(5L, null, OrderStatus.PAGAMENTO_RECUSADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.CANCELADO);

            when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            useCase.updateOrderStatus(5L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELADO);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve atualizar status PAGAMENTO_RECUSADO -> PAGAMENTO_PENDENTE")
        void shouldUpdateStatusRecusadoToPendente() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(6L, null, OrderStatus.PAGAMENTO_RECUSADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            useCase.updateOrderStatus(6L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve atualizar status PAGAMENTO_APROVADO -> CONCLUIDO")
        void shouldUpdateStatusAprovadoToConcluido() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Item", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(4L, null, OrderStatus.PAGAMENTO_APROVADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.CONCLUIDO);

            when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            useCase.updateOrderStatus(4L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONCLUIDO);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve lançar erro quando pedido está vazio")
        void shouldThrowWhenOrderIsEmpty() {
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>(),  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Não é possível mudar status de um pedido sem itens");
        }

        @Test
        @DisplayName("Deve lançar erro se pedido não existir")
        void shouldThrowIfOrderNotFoundWhenUpdatingStatus() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            assertThatThrownBy(() -> useCase.updateOrderStatus(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve falhar com transição inválida INICIADO -> CONCLUIDO")
        void shouldFailInvalidTransitionIniciadoToConcluido() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.CONCLUIDO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição de status inválida");
        }

        @Test
        @DisplayName("Deve falhar com transição inválida INICIADO -> PAGAMENTO_APROVADO")
        void shouldFailInvalidTransitionIniciadoToAprovado() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_APROVADO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição de status inválida");
        }

        @Test
        @DisplayName("Deve falhar com transição inválida PAGAMENTO_APROVADO -> PAGAMENTO_PENDENTE")
        void shouldFailInvalidTransitionAprovadoToPendente() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.PAGAMENTO_APROVADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição de status inválida");
        }

        @Test
        @DisplayName("Deve falhar com transição inválida CONCLUIDO -> INICIADO")
        void shouldFailInvalidTransitionConcluidoToIniciado() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.CONCLUIDO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.INICIADO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição de status inválida");
        }

        @Test
        @DisplayName("Deve falhar com transição inválida PAGAMENTO_RECUSADO -> CONCLUIDO")
        void shouldFailInvalidTransitionRecusadoToConcluido() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.PAGAMENTO_RECUSADO, items,  null, null);
            var request = new OrderStatusUpdateRequest(OrderStatus.CONCLUIDO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> useCase.updateOrderStatus(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição de status inválida");
        }
    }

    @Nested
    @DisplayName("Cenários de Listagem")
    class ListOrdersTests {
        @Test
        @DisplayName("Deve listar pedidos filtrados por status")
        void shouldListOrdersByFilter() {
            var statusList = List.of(OrderStatus.INICIADO);
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>(),  null, null);

            when(orderRepository.findByFilters(statusList)).thenReturn(List.of(order));
            when(orderMapper.toResponse(order)).thenReturn(mock(OrderResponse.class));

            var result = useCase.listAllOrdersByFilters(statusList);

            assertThat(result).hasSize(1);
            verify(orderRepository).findByFilters(statusList);
        }

        @Test
        @DisplayName("Deve listar pedidos com múltiplos filtros de status")
        void shouldListOrdersByMultipleFilters() {
            var statusList = List.of(OrderStatus.INICIADO, OrderStatus.PAGAMENTO_PENDENTE);
            var order1 = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>(),  null, null);
            var order2 = new OrderDefinition(2L, null, OrderStatus.PAGAMENTO_PENDENTE, new ArrayList<>(),  null, null);

            when(orderRepository.findByFilters(statusList)).thenReturn(List.of(order1, order2));
            when(orderMapper.toResponse(order1)).thenReturn(mock(OrderResponse.class));
            when(orderMapper.toResponse(order2)).thenReturn(mock(OrderResponse.class));

            var result = useCase.listAllOrdersByFilters(statusList);

            assertThat(result).hasSize(2);
            verify(orderRepository).findByFilters(statusList);
        }

        @Test
        @DisplayName("Deve listar pedidos sem filtros (null)")
        void shouldListOrdersWithoutFilters() {
            var order1 = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>(),  null, null);
            var order2 = new OrderDefinition(2L, null, OrderStatus.PAGAMENTO_PENDENTE, new ArrayList<>(),  null, null);

            when(orderRepository.findByFilters(null)).thenReturn(List.of(order1, order2));
            when(orderMapper.toResponse(order1)).thenReturn(mock(OrderResponse.class));
            when(orderMapper.toResponse(order2)).thenReturn(mock(OrderResponse.class));

            var result = useCase.listAllOrdersByFilters(null);

            assertThat(result).hasSize(2);
            verify(orderRepository).findByFilters(null);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos")
        void shouldReturnEmptyListWhenNoOrders() {
            when(orderRepository.findByFilters(null)).thenReturn(List.of());

            var result = useCase.listAllOrdersByFilters(null);

            assertThat(result).isEmpty();
            verify(orderRepository).findByFilters(null);
        }
    }
}
