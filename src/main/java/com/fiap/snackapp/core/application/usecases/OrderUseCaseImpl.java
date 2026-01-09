package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.*;
import com.fiap.snackapp.core.application.dto.response.OrderPaymentCreatedMessageResponse;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ProductRepositoryPort productRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddOnRepositoryPort addOnRepository;
    private final RabbitTemplate rabbitTemplate;

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

            List<AppliedAddOn> appliedAddOns =
                    (itemReq.addOns() == null)
                            ? new ArrayList<>()
                            : itemReq.addOns().stream()
                            .map(addOnReq -> {
                                AddOnDefinition addOn = addOnRepository.findById(addOnReq.addOnId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Adicional não encontrado: " + addOnReq.addOnId()));
                                return new AppliedAddOn(
                                        new AddOnDefinition(addOn.id(), addOn.name(), addOn.category(), addOn.price()),
                                        addOnReq.quantity()
                                );
                            })
                            .collect(Collectors.toCollection(ArrayList::new));

            OrderItemDefinition item = orderItemMapper.toDomain(product, itemReq, appliedAddOns);
            order.addItem(item);
        }

        OrderDefinition updated = orderRepository.save(order);
        return orderMapper.toResponse(updated);
    }

    @Override
    public void requestOrderPaymentCreation(OrderPaymentCreateRequest orderPaymentCreateRequest) {
        updateOrderStatus(orderPaymentCreateRequest.orderId(),
                new OrderStatusUpdateRequest(OrderStatus.PAGAMENTO_PENDENTE));

        rabbitTemplate.convertAndSend(
                "payment.exchange",
                "payment.create",
                orderPaymentCreateRequest
        );
    }

    @Override
    public void updateOrderWithQrCode(OrderPaymentCreatedMessageResponse response) {
        OrderDefinition order = orderRepository.findById(response.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + response.orderId()));
        order.setQrCodeUrl(response.qrCodeUrl());
        order.setPaymentId(response.paymentId());
        orderRepository.save(order);
    }

    @Override
    public void sendOrderToKitchen(OrderDefinition order) {
        OrderToKitchenRequest orderToKitchen = orderMapper.toKitchenRequest(order);
        rabbitTemplate.convertAndSend("kitchen.order.received", orderToKitchen);
    }

    @Override
    public OrderDefinition updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
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
        return orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> listAllOrdersByFilters(List<OrderStatus> orderStatus) {
        return orderRepository.findByFilters(orderStatus)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse listOrderById(Long orderId) {
        var result = orderRepository.findById(orderId);

        return result.map(orderMapper::toResponse).orElse(null);
    }

    private boolean isNextValid(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case INICIADO -> next == OrderStatus.PAGAMENTO_PENDENTE;
            case PAGAMENTO_PENDENTE -> next == OrderStatus.PAGAMENTO_APROVADO || next == OrderStatus.PAGAMENTO_RECUSADO;
            case PAGAMENTO_RECUSADO -> next == OrderStatus.CANCELADO || next == OrderStatus.PAGAMENTO_PENDENTE;
            case PAGAMENTO_APROVADO -> next == OrderStatus.CONCLUIDO;
            default -> false;
        };
    }
}