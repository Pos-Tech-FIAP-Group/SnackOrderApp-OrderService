package com.fiap.snackapp.adapters.driver.messaging;

import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderPaymentStatusUpdatedMessage;
import com.fiap.snackapp.core.application.usecases.OrderUseCase;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPaymentStatusUpdatedListener {
    private final OrderUseCase orderUseCase;

    @RabbitListener(queues = "order.payment.status.queue")
    public void handlePaymentStatusUpdated(OrderPaymentStatusUpdatedMessage message) {
        OrderDefinition order = orderUseCase.updateOrderStatus(message.orderId(), new OrderStatusUpdateRequest(message.status()));
        if (order.getStatus().equals(OrderStatus.PAGAMENTO_APROVADO)) {
            orderUseCase.sendOrderToKitchen(order);
        } else if (order.getStatus().equals(OrderStatus.PAGAMENTO_RECUSADO)) {
            orderUseCase.updateOrderStatus(message.orderId(), new OrderStatusUpdateRequest(OrderStatus.CANCELADO));
        }
    }
}
