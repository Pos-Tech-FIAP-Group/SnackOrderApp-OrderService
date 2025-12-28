package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;
import com.fiap.snackapp.core.application.mapper.CustomerMapper;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseImplTest {

    @Mock
    CustomerRepositoryPort userRepository;

    @Mock
    CustomerMapper customerMapper;

    @InjectMocks
    CustomerUseCaseImpl useCase;

    @Test
    @DisplayName("Deve criar usuário (mapeia request, salva e retorna response)")
    void shouldCreateUser() {
        // arrange
        var request = mock(CustomerCreateRequest.class);

        var domainUser = mock(CustomerDefinition.class);
        var savedUser = mock(CustomerDefinition.class);
        var expectedResponse = mock(CustomerResponse.class);

        when(customerMapper.toDomain(request)).thenReturn(domainUser);
        when(userRepository.save(domainUser)).thenReturn(savedUser);
        when(customerMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // act
        var response = useCase.createUser(request);

        // assert
        assertThat(response).isSameAs(expectedResponse);

        verify(customerMapper).toDomain(request);
        verify(userRepository).save(domainUser);
        verify(customerMapper).toResponse(savedUser);

        verifyNoMoreInteractions(customerMapper, userRepository);
    }

    @Test
    @DisplayName("Deve salvar exatamente o CustomerDefinition gerado pelo mapper")
    void shouldSaveMappedDomainUser() {
        // arrange
        var request = mock(CustomerCreateRequest.class);
        var domainUser = mock(CustomerDefinition.class);
        var savedUser = mock(CustomerDefinition.class);

        when(customerMapper.toDomain(request)).thenReturn(domainUser);
        when(userRepository.save(any(CustomerDefinition.class))).thenReturn(savedUser);
        when(customerMapper.toResponse(savedUser)).thenReturn(mock(CustomerResponse.class));

        // act
        useCase.createUser(request);

        // assert (captura o argumento do save)
        var captor = ArgumentCaptor.forClass(CustomerDefinition.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(domainUser);
    }
}
