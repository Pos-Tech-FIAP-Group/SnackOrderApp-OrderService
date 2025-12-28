package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.OrderInitRequest;
import com.fiap.snackapp.core.application.dto.request.OrderItemsRequest;
import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.domain.enums.OrderStatus;

import java.util.List;

public interface OrderUseCase {
    OrderResponse initOrder(String cpf);
    OrderResponse addItems(Long orderId, OrderItemsRequest request);
    void updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
    List<OrderResponse> listAllOrdersByFilters(List<OrderStatus> orderStatus);
}
