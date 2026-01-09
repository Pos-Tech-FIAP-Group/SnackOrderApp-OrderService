package com.fiap.snackapp.adapters.driver.messaging;

import com.fiap.snackapp.core.application.dto.response.OrderPaymentCreatedMessageResponse;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPaymentCreatedListener {
    private final OrderUseCase orderUseCase;

    @RabbitListener(queues = "payment.created.queue")
    public void handleOrderPaymentCreated(OrderPaymentCreatedMessageResponse orderPaymentCreatedMessageResponse) {
        orderUseCase.updateOrderWithQrCode(orderPaymentCreatedMessageResponse);
    }
}
