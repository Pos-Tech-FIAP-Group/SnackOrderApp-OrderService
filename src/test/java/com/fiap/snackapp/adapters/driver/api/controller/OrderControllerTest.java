package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.OrderInitRequest;
import com.fiap.snackapp.core.application.dto.request.OrderItemsRequest;
import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("POST /api/orders/init: deve priorizar CPF do header e retornar 201")
    void init_shouldUseCpfFromHeader_whenHeaderPresent() {
        var request = mock(OrderInitRequest.class); // não precisa stubbar cpf()

        when(httpServletRequest.getHeader("X-CPF")).thenReturn("12345678900");

        var expected = mock(OrderResponse.class);
        when(orderUseCase.initOrder("12345678900")).thenReturn(expected);

        var response = controller.init(request, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expected);

        verify(orderUseCase).initOrder("12345678900");
        verifyNoMoreInteractions(orderUseCase);
    }

    @Test
    @DisplayName("POST /api/orders/init: deve usar CPF do body quando header ausente/vazio e retornar 201")
    void init_shouldFallbackToBodyCpf_whenHeaderBlank() {
        var request = mock(OrderInitRequest.class);
        when(request.cpf()).thenReturn("12345678900");

        when(httpServletRequest.getHeader("X-CPF")).thenReturn("   "); // blank

        var expected = mock(OrderResponse.class);
        when(orderUseCase.initOrder("12345678900")).thenReturn(expected);

        var response = controller.init(request, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expected);

        verify(orderUseCase).initOrder("12345678900");
        verifyNoMoreInteractions(orderUseCase);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/item: deve delegar e retornar 200")
    void addItems_shouldReturnOk() {
        var request = mock(OrderItemsRequest.class);
        var expected = mock(OrderResponse.class);

        when(orderUseCase.addItems(10L, request)).thenReturn(expected);

        var response = controller.addItems(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);

        verify(orderUseCase).addItems(10L, request);
        verifyNoMoreInteractions(orderUseCase);
    }

    @Test
    @DisplayName("PATCH /api/orders/{orderId}/status: deve delegar e retornar 202 sem body")
    void updateOrderStatus_shouldReturnAccepted() {
        var request = mock(OrderStatusUpdateRequest.class);

        var response = controller.updateOrderStatus(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(orderUseCase).updateOrderStatus(10L, request);
        verifyNoMoreInteractions(orderUseCase);
    }

    @Test
    @DisplayName("GET /api/orders: deve delegar filtros e retornar 200")
    void listAllOrdersByFilters_shouldReturnOk() {
        List<OrderStatus> filters = List.of(OrderStatus.INICIADO);
        var expectedList = List.of(mock(OrderResponse.class));

        when(orderUseCase.listAllOrdersByFilters(filters)).thenReturn(expectedList);

        var response = controller.listAllOrdersByFilters(filters);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedList);

        verify(orderUseCase).listAllOrdersByFilters(filters);
        verifyNoMoreInteractions(orderUseCase);
    }
}
