package com.fiap.snackapp.bdd.steps;

import com.fiap.snackapp.core.application.dto.response.OrderResponse;
import com.fiap.snackapp.core.application.mapper.OrderItemMapper;
import com.fiap.snackapp.core.application.mapper.OrderMapper;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.application.repository.OrderRepositoryPort;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.application.usecases.OrderUseCaseImpl;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.model.OrderDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InitOrderSteps {

    @Mock private OrderRepositoryPort orderRepository;
    @Mock private CustomerRepositoryPort customerRepository;
    @Mock private OrderMapper orderMapper;

    @Mock private ProductRepositoryPort productRepository;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private AddOnRepositoryPort addOnRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderUseCaseImpl orderUseCase;

    private String cpfInput;
    private OrderResponse response;
    private OrderDefinition orderDefinitionMock;

    public InitOrderSteps() {
        MockitoAnnotations.openMocks(this);
    }

    @Dado("que possuo um CPF válido {string}")
    public void quePossuoUmCPFValido(String cpf) {
        this.cpfInput = cpf;
    }

    @E("que o cliente com este CPF já está cadastrado na base")
    public void queOClienteComEsteCPFJaEstaCadastradoNaBase() {
        CustomerDefinition customerMock = mock(CustomerDefinition.class);
        when(customerRepository.findByCpf(any(CPF.class))).thenReturn(Optional.of(customerMock));
    }

    @Quando("eu solicito o início de um pedido")
    public void euSolicitoOInicioDeUmPedido() {
        // PREPARAÇÃO DOS MOCKS DO MAPPER E REPO DE PEDIDOS

        // 1. O mapper converte Customer -> OrderDefinition
        orderDefinitionMock = mock(OrderDefinition.class);
        when(orderMapper.toOrderDomain(any(CustomerDefinition.class))).thenReturn(orderDefinitionMock);

        // 2. O repositório salva o OrderDefinition e retorna ele mesmo (simulando persistência)
        when(orderRepository.save(any(OrderDefinition.class))).thenReturn(orderDefinitionMock);

        // 3. O mapper converte OrderDefinition -> OrderResponse
        OrderResponse responseMock = mock(OrderResponse.class); // Mock genérico da resposta
        when(orderMapper.toResponse(any(OrderDefinition.class))).thenReturn(responseMock);

        // AÇÃO
        response = orderUseCase.initOrder(cpfInput);
    }

    @Entao("um novo pedido deve ser salvo no repositório")
    public void umNovoPedidoDeveSerSalvoNoRepositorio() {
        // Verifica se o método save foi chamado 1 vez
        verify(orderRepository, times(1)).save(any(OrderDefinition.class));
    }

    @E("o sistema deve retornar a resposta com os dados do pedido")
    public void oSistemaDeveRetornarARespostaComOsDadosDoPedido() {
        // Garante que o método retornou algo diferente de nulo
        assertNotNull(response);

        // Verifica se o fluxo passou pelo mapper final
        verify(orderMapper, times(1)).toResponse(any(OrderDefinition.class));
    }
}