package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.OrderItemsRequest;
import com.fiap.snackapp.core.application.dto.request.OrderPaymentCreateRequest;
import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderPaymentCreatedMessageResponse;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.OrderDefinition;

import java.util.List;

public interface OrderUseCase {
    OrderResponse initOrder(String cpf);

    OrderResponse addItems(Long orderId, OrderItemsRequest request);

    OrderDefinition updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);

    List<OrderResponse> listAllOrdersByFilters(List<OrderStatus> orderStatus);

    OrderResponse listOrderById(Long orderId);

    void requestOrderPaymentCreation(OrderPaymentCreateRequest orderPaymentCreateRequest);

    void updateOrderWithQrCode(OrderPaymentCreatedMessageResponse orderPaymentCreatedMessageResponse);

    void sendOrderToKitchen(OrderDefinition order);
}
