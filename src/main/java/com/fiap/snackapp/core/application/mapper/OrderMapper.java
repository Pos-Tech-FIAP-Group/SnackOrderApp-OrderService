package com.fiap.snackapp.core.application.mapper;

import com.fiap.snackapp.core.application.dto.request.ItemToKitchenRequest;
import com.fiap.snackapp.core.application.dto.request.OrderInitRequest;
import com.fiap.snackapp.core.application.dto.request.OrderToKitchenRequest;
import com.fiap.snackapp.core.application.dto.response.OrderItemResponse;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public CustomerDefinition toCustomerDomain(OrderInitRequest request) {
        if (request == null || request.cpf() == null || request.cpf().isBlank()) {
            return null;
        }

        CPF cpf = new CPF(request.cpf());
        return new CustomerDefinition(null, null, null, cpf);
    }

    public OrderDefinition toOrderDomain(CustomerDefinition customer) {
        return new OrderDefinition(null, customer, OrderStatus.INICIADO, new ArrayList<>(), null, null);
    }

    public OrderResponse toResponse(OrderDefinition order) {
        String cpf = (order.getCustomer() != null) ? order.getCustomer().cpf().toString() : null;

        List<OrderItemResponse> items = order.getItems().stream()
                .map(orderItemMapper::toResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                cpf,
                items,
                order.getTotalPrice(),
                order.getQrCodeUrl(),
                order.getPaymentId()
        );
    }

    public OrderToKitchenRequest toKitchenRequest(OrderDefinition order) {
        List<ItemToKitchenRequest> items = order.getItems().stream()
                .map(orderItemMapper::toKitchenRequest)
                .toList();

        return new OrderToKitchenRequest(order.getId(), items);
    }

}
