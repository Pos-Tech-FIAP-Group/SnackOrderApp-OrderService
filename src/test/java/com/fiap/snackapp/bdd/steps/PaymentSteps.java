package com.fiap.snackapp.bdd.steps;

import com.fiap.snackapp.core.application.dto.request.OrderPaymentCreateRequest;
import com.fiap.snackapp.core.application.repository.OrderRepositoryPort;
import com.fiap.snackapp.core.application.usecases.OrderUseCaseImpl;
import com.fiap.snackapp.core.domain.enums.OrderStatus;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import com.fiap.snackapp.core.domain.model.OrderItemDefinition;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

public class PaymentSteps {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderUseCaseImpl orderUseCase;

    private OrderDefinition orderMock;
    private OrderPaymentCreateRequest paymentRequest;

    public PaymentSteps() {
        MockitoAnnotations.openMocks(this);
    }

    @Dado("que existe um pedido com id {long} no status {string} contendo itens")
    public void queExisteUmPedidoComIdNoStatusContendoItens(Long id, String statusStr) {
        List<OrderItemDefinition> items = new ArrayList<>();
        items.add(mock(OrderItemDefinition.class));

        orderMock = new OrderDefinition(
                id,
                mock(CustomerDefinition.class),
                OrderStatus.valueOf(statusStr),
                items,
                null,
                null
        );

        when(orderRepository.findById(id)).thenReturn(Optional.of(orderMock));
        when(orderRepository.save(any(OrderDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Quando("eu solicito a criação do pagamento para o pedido {long}")
    public void euSolicitoACriacaoDoPagamentoParaOPedido(Long id) {
        paymentRequest = new OrderPaymentCreateRequest(
                id,
                new BigDecimal("50.00"),
                1L
        );

        orderUseCase.requestOrderPaymentCreation(paymentRequest);
    }

    @Entao("o status do pedido deve ser atualizado para {string}")
    public void oStatusDoPedidoDeveSerAtualizadoPara(String novoStatusStr) {
        verify(orderRepository, times(1)).save(any(OrderDefinition.class));

        assertEquals(OrderStatus.valueOf(novoStatusStr), orderMock.getStatus());
    }

    @E("uma mensagem de criação de pagamento deve ser enviada para a fila")
    public void umaMensagemDeCriacaoDePagamentoDeveSerEnviadaParaAFila() {
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("payment.exchange"),
                eq("payment.create"),
                eq(paymentRequest)
        );
    }
}