package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;
import com.fiap.snackapp.core.application.mapper.CustomerMapper;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerUseCaseImpl useCase;

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void shouldCreateCustomerSuccessfully() {
        // Dados de entrada
        var request = new CustomerCreateRequest("João", "joao@email.com", "12345678900");

        // Objetos de domínio (Mockados)
        var domainUser = new CustomerDefinition(
                null,
                "João",
                new Email("joao@email.com"),
                new CPF("12345678900")
        );

        var savedUser = new CustomerDefinition(
                1L, // ID gerado
                "João",
                new Email("joao@email.com"),
                new CPF("12345678900")
        );

        var expectedResponse = new CustomerResponse(1L, "João", "joao@email.com", "12345678900");

        // Configuração dos Mocks
        when(customerMapper.toDomain(request)).thenReturn(domainUser);
        when(customerRepository.save(domainUser)).thenReturn(savedUser);
        when(customerMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Execução
        var result = useCase.createUser(request);

        // Verificações
        assertThat(result).isEqualTo(expectedResponse);

        verify(customerMapper).toDomain(request);
        verify(customerRepository).save(domainUser);
        verify(customerMapper).toResponse(savedUser);
    }
}
