package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.OrderInitRequest;
import com.fiap.snackapp.core.application.dto.request.OrderItemsRequest;
import com.fiap.snackapp.core.application.dto.request.OrderPaymentCreateRequest;
import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderController controller;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Nested
    @DisplayName("POST /api/orders/init")
    class InitOrderTests {
        @Test
        @DisplayName("deve priorizar CPF do header quando presente")
        void init_shouldUseCpfFromHeader_whenHeaderPresent() {
            var request = mock(OrderInitRequest.class);
            when(httpServletRequest.getHeader("X-CPF")).thenReturn("12345678900");

            var expected = mock(OrderResponse.class);
            when(orderUseCase.initOrder("12345678900")).thenReturn(expected);

            var response = controller.init(request, httpServletRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(expected);
            verify(orderUseCase).initOrder("12345678900");
        }

        @Test
        @DisplayName("deve usar CPF do body quando header ausente ou vazio")
        void init_shouldFallbackToBodyCpf_whenHeaderBlank() {
            var request = mock(OrderInitRequest.class);
            when(request.cpf()).thenReturn("12345678900");
            when(httpServletRequest.getHeader("X-CPF")).thenReturn("   ");

            var expected = mock(OrderResponse.class);
            when(orderUseCase.initOrder("12345678900")).thenReturn(expected);

            var response = controller.init(request, httpServletRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(expected);
            verify(orderUseCase).initOrder("12345678900");
        }

        @Test
        @DisplayName("deve usar CPF do body quando header é null")
        void init_shouldFallbackToBodyCpf_whenHeaderNull() {
            var request = mock(OrderInitRequest.class);
            when(request.cpf()).thenReturn("12345678900");
            when(httpServletRequest.getHeader("X-CPF")).thenReturn(null);

            var expected = mock(OrderResponse.class);
            when(orderUseCase.initOrder("12345678900")).thenReturn(expected);

            var response = controller.init(request, httpServletRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(expected);
            verify(orderUseCase).initOrder("12345678900");
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{orderId}/item")
    class AddItemsTests {
        @Test
        @DisplayName("deve adicionar itens ao pedido e retornar 200")
        void addItems_shouldReturnOk() {
            var request = mock(OrderItemsRequest.class);
            var expected = mock(OrderResponse.class);
            when(orderUseCase.addItems(10L, request)).thenReturn(expected);

            var response = controller.addItems(10L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(orderUseCase).addItems(10L, request);
        }
    }

    @Nested
    @DisplayName("POST /api/orders (Payment Creation)")
    class RequestOrderPaymentCreationTests {
        @Test
        @DisplayName("deve solicitar criação de pagamento e retornar 202 sem body")
        void requestOrderPaymentCreation_shouldReturnAccepted() {
            var request = mock(OrderPaymentCreateRequest.class);

            var response = controller.requestOrderPaymentCreation(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            assertThat(response.getBody()).isNull();
            verify(orderUseCase).requestOrderPaymentCreation(request);
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/{orderId}/status")
    class UpdateOrderStatusTests {
        @Test
        @DisplayName("deve atualizar status do pedido e retornar 204")
        void updateOrderStatus_shouldReturnNoContent() {
            var request = mock(OrderStatusUpdateRequest.class);

            var response = controller.updateOrderStatus(10L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
            verify(orderUseCase).updateOrderStatus(10L, request);
        }
    }

    @Nested
    @DisplayName("GET /api/orders (List by Filters)")
    class ListAllOrdersByFiltersTests {
        @Test
        @DisplayName("deve listar todos os pedidos com filtros e retornar 200")
        void listAllOrdersByFilters_shouldReturnOk() {
            var filters = List.of(OrderStatus.INICIADO);
            var expectedList = List.of(mock(OrderResponse.class));
            when(orderUseCase.listAllOrdersByFilters(filters)).thenReturn(expectedList);

            var response = controller.listAllOrdersByFilters(filters);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expectedList);
            verify(orderUseCase).listAllOrdersByFilters(filters);
        }

        @Test
        @DisplayName("deve listar todos os pedidos sem filtros (null)")
        void listAllOrdersByFilters_shouldReturnOkWithoutFilters() {
            var expectedList = List.of(mock(OrderResponse.class), mock(OrderResponse.class));
            when(orderUseCase.listAllOrdersByFilters(null)).thenReturn(expectedList);

            var response = controller.listAllOrdersByFilters(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            verify(orderUseCase).listAllOrdersByFilters(null);
        }
    }
}
