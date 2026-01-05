package com.fiap.snackapp.adapters.driver.messaging;

import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderPaymentStatusUpdatedMessage;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderPaymentStatusUpdatedListenerTest {

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderPaymentStatusUpdatedListener listener;

    @Test
    @DisplayName("Deve atualizar status do pedido quando receber mensagem de pagamento")
    void shouldHandlePaymentStatusUpdated() {
        // Arrange
        Long orderId = 123L;
        String paymentId = "pay-xyz-987";
        OrderStatus newStatus = OrderStatus.PAGAMENTO_APROVADO;

        var message = new OrderPaymentStatusUpdatedMessage(orderId, paymentId, newStatus);

        // Act
        listener.handlePaymentStatusUpdated(message);

        // Assert
        ArgumentCaptor<OrderStatusUpdateRequest> captor = ArgumentCaptor.forClass(OrderStatusUpdateRequest.class);

        verify(orderUseCase).updateOrderStatus(eq(orderId), captor.capture());

        assertEquals(newStatus, captor.getValue().status());
    }
}
