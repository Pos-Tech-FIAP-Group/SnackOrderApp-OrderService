package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.ItemRequest;
import com.fiap.snackapp.core.application.dto.request.OrderItemsRequest;
import com.fiap.snackapp.core.application.dto.request.OrderStatusUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.mapper.OrderItemMapper;
import com.fiap.snackapp.core.application.mapper.OrderMapper;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.application.repository.OrderRepositoryPort;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.*;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ProductRepositoryPort productRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddOnRepositoryPort addOnRepository;

    @Override
    public OrderResponse initOrder(String cpf) {
        CustomerDefinition customer = null;
        CPF inputCpf = new CPF(cpf);

        customer = customerRepository.findByCpf(inputCpf)
                .orElseGet(() -> customerRepository.save(new CustomerDefinition(null, "Cliente", new Email("default@email.com"), inputCpf)));

        OrderDefinition order = orderMapper.toOrderDomain(customer);
        OrderDefinition savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse addItems(Long orderId, OrderItemsRequest request) {
        OrderDefinition order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + orderId));

        for (ItemRequest itemReq : request.items()) {
            ProductDefinition product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + itemReq.productId()));

            List<AppliedAddOn> appliedAddOns = itemReq.addOns() != null
                    ? itemReq.addOns().stream()
                    .map(addOnReq -> {
                        AddOnDefinition addOn = addOnRepository.findById(addOnReq.addOnId())
                                .orElseThrow(() -> new ResourceNotFoundException("Adicional não encontrado: " + addOnReq.addOnId()));
                        return new AppliedAddOn(
                                new AddOnDefinition(addOn.id(), addOn.name(), addOn.category(), addOn.price()),
                                addOnReq.quantity()
                        );
                    })
                    .toList()
                    : List.of();

            OrderItemDefinition item = orderItemMapper.toDomain(product, itemReq, appliedAddOns);

            order.addItem(item);
        }

        OrderDefinition updated = orderRepository.save(order);
        return orderMapper.toResponse(updated);
    }


    @Override
    public void updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        OrderDefinition order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + orderId));

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Não é possível mudar status de um pedido sem itens.");
        }

        OrderStatus current = order.getStatus();
        OrderStatus next = request.status();

        if (!isNextValid(current, next)) {
            throw new IllegalStateException("Transição de status inválida: " + current + " → " + next);
        }

        order.setStatus(next);
        orderRepository.save(order);

    }

    @Override
    public List<OrderResponse> listAllOrdersByFilters(List<OrderStatus> orderStatus) {
        return orderRepository.findByFilters(orderStatus)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    private boolean isNextValid(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case INICIADO -> next == OrderStatus.RECEBIDO;
            case RECEBIDO -> next == OrderStatus.EM_PREPARACAO;
            case EM_PREPARACAO -> next == OrderStatus.PRONTO;
            case PRONTO -> next == OrderStatus.FINALIZADO;
            default -> false;
        };
    }
}