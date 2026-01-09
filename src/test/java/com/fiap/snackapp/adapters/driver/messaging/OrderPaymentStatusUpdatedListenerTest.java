package com.fiap.snackapp.adapters.driver.messaging;

import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderPaymentStatusUpdatedMessage;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPaymentStatusUpdatedListenerTest {

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderPaymentStatusUpdatedListener listener;

    @Test
    @DisplayName("Deve enviar pedido para cozinha quando pagamento for APROVADO")
    void shouldSendToKitchenWhenPaymentIsApproved() {
        // Arrange
        Long orderId = 123L;
        String paymentId = "pay-apr-001";

        // Mensagem de entrada
        var message = new OrderPaymentStatusUpdatedMessage(orderId, paymentId, OrderStatus.PAGAMENTO_APROVADO);

        OrderDefinition approvedOrder = new OrderDefinition(orderId, null, OrderStatus.PAGAMENTO_APROVADO, null,  null, null);

        when(orderUseCase.updateOrderStatus(eq(orderId), any(OrderStatusUpdateRequest.class)))
                .thenReturn(approvedOrder);

        // Act
        listener.handlePaymentStatusUpdated(message);

        // Assert
        // 1. Verifica se atualizou o status inicial
        verify(orderUseCase).updateOrderStatus(eq(orderId), argThat(req -> req.status() == OrderStatus.PAGAMENTO_APROVADO));

        // 2. Verifica se enviou para a cozinha
        verify(orderUseCase).sendOrderToKitchen(approvedOrder);
    }

    @Test
    @DisplayName("Deve mudar status para CANCELADO quando pagamento for RECUSADO")
    void shouldCancelOrderWhenPaymentIsRefused() {
        // Arrange
        Long orderId = 456L;
        String paymentId = "pay-rec-002";

        // Mensagem de entrada
        var message = new OrderPaymentStatusUpdatedMessage(orderId, paymentId, OrderStatus.PAGAMENTO_RECUSADO);

        OrderDefinition refusedOrder = new OrderDefinition(orderId, null, OrderStatus.PAGAMENTO_RECUSADO, null,  null, null);

        when(orderUseCase.updateOrderStatus(eq(orderId), any(OrderStatusUpdateRequest.class)))
                .thenReturn(refusedOrder);

        // Act
        listener.handlePaymentStatusUpdated(message);

        // Assert
        ArgumentCaptor<OrderStatusUpdateRequest> captor = ArgumentCaptor.forClass(OrderStatusUpdateRequest.class);

        // Deve chamar o updateOrderStatus 2 vezes:
        // 1ª vez: Atualiza para PAGAMENTO_RECUSADO (vindo da mensagem)
        // 2ª vez: Atualiza para CANCELADO (lógica do listener)
        verify(orderUseCase, times(2)).updateOrderStatus(eq(orderId), captor.capture());

        // Valida a ordem das chamadas
        List<OrderStatusUpdateRequest> capturedRequests = captor.getAllValues();
        assertEquals(OrderStatus.PAGAMENTO_RECUSADO, capturedRequests.get(0).status());
        assertEquals(OrderStatus.CANCELADO, capturedRequests.get(1).status());

        // Garante que NÃO enviou para a cozinha
        verify(orderUseCase, never()).sendOrderToKitchen(any());
    }
}
