package com.fiap.snackapp.adapters.driver.messaging;

import com.fiap.snackapp.core.application.dto.response.OrderPaymentCreatedMessageResponse;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderPaymentCreatedListenerTest {

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderPaymentCreatedListener listener;

    @Test
    @DisplayName("Deve processar mensagem de pagamento criado chamando o UseCase")
    void shouldHandleOrderPaymentCreated() {
        // Arrange
        var message = new OrderPaymentCreatedMessageResponse(
                "pay-123",
                10L,
                BigDecimal.valueOf(50.00),
                "http://qrcode.url",
                OrderStatus.PAGAMENTO_PENDENTE
        );

        // Act
        // Chamamos o método diretamente como se fosse o framework RabbitMQ invocando
        listener.handleOrderPaymentCreated(message);

        // Assert
        // Verificamos se o UseCase foi chamado com a mensagem correta
        verify(orderUseCase).updateOrderWithQrCode(message);
    }
}
