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

            var orderToSave = new OrderDefinition(null, savedCustomer, OrderStatus.INICIADO, new ArrayList<>());
            var savedOrder = new OrderDefinition(10L, savedCustomer, OrderStatus.INICIADO, new ArrayList<>());
            var expectedResponse = new OrderResponse(10L, OrderStatus.INICIADO.name(), cpfString, List.of(), BigDecimal.ZERO);

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
            var savedOrder = new OrderDefinition(10L, existingCustomer, OrderStatus.INICIADO, new ArrayList<>());
            var expectedResponse = new OrderResponse(10L, OrderStatus.INICIADO.name(), cpfString, List.of(), BigDecimal.ZERO);

            when(customerRepository.findByCpf(any(CPF.class))).thenReturn(Optional.of(existingCustomer));
            when(orderMapper.toOrderDomain(existingCustomer)).thenReturn(new OrderDefinition(null, existingCustomer, OrderStatus.INICIADO, new ArrayList<>()));
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

            var itemDomain = new OrderItemDefinition(prodId, "Lanche", 1, BigDecimal.valueOf(14), new ArrayList<>());
            when(orderItemMapper.toDomain(eq(product), eq(itemRequest), anyList())).thenReturn(itemDomain);

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
            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, new ArrayList<>());
            var product = new ProductDefinition(10L, "Lanche", Category.LANCHE, BigDecimal.TEN, "Desc", true);

            var itemRequest = new ItemRequest(10L, 1, null); // AddOns NULL
            var request = new OrderItemsRequest(List.of(itemRequest));

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));

            // Mapper deve ser chamado com lista vazia
            when(orderItemMapper.toDomain(eq(product), eq(itemRequest), eq(List.of())))
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
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>());
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
            var order = new OrderDefinition(orderId, null, OrderStatus.INICIADO, new ArrayList<>());
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
        @DisplayName("Deve enviar requisição de pagamento para fila correta")
        void shouldSendPaymentRequestToQueue() {
            var request = new OrderPaymentCreateRequest(1L, BigDecimal.TEN, 99L);

            useCase.requestOrderPaymentCreation(request);

            verify(rabbitTemplate).convertAndSend(
                    eq("payment.exchange"),
                    eq("payment.create"),
                    eq(request)
            );
        }

        @Test
        @DisplayName("Deve enviar pedido para cozinha quando pagamento aprovado")
        void shouldSendOrderToKitchenWhenApproved() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Dummy", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(1L, null, OrderStatus.PAGAMENTO_PENDENTE, items);

            var request = new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_APROVADO);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderMapper.toKitchenRequest(order)).thenReturn(new OrderToKitchenRequest(1L, List.of()));

            useCase.updateOrderStatus(1L, request);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_APROVADO);
            verify(rabbitTemplate).convertAndSend(eq("kitchen.order.received"), any(OrderToKitchenRequest.class));
        }

        @Test
        @DisplayName("Deve processar callback de QR Code")
        void shouldProcessQrCodeCallback() {
            var items = new ArrayList<OrderItemDefinition>();
            items.add(new OrderItemDefinition(10L, "Item", 1, BigDecimal.TEN, new ArrayList<>()));
            var order = new OrderDefinition(55L, null, OrderStatus.INICIADO, items);

            when(orderRepository.findById(55L)).thenReturn(Optional.of(order));

            var responseMsg = new OrderPaymentCreatedMessageResponse(
                    "pay-uuid", 55L, BigDecimal.TEN, "http://qr.code", OrderStatus.PAGAMENTO_PENDENTE
            );

            useCase.updateOrderWithQrCode(responseMsg);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAGAMENTO_PENDENTE);
            verify(orderRepository).save(order);
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

    @Nested
    @DisplayName("Cenários de Listagem")
    class ListOrdersTests {
        @Test
        @DisplayName("Deve listar pedidos filtrados por status")
        void shouldListOrdersByFilter() {
            var statusList = List.of(OrderStatus.INICIADO);
            var order = new OrderDefinition(1L, null, OrderStatus.INICIADO, new ArrayList<>());

            when(orderRepository.findByFilters(statusList)).thenReturn(List.of(order));
            when(orderMapper.toResponse(order)).thenReturn(mock(OrderResponse.class));

            var result = useCase.listAllOrdersByFilters(statusList);

            assertThat(result).hasSize(1);
            verify(orderRepository).findByFilters(statusList);
        }
    }
}
