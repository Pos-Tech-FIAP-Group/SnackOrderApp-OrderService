package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;
import com.fiap.snackapp.core.application.usecases.CustomerUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerUseCase customerUseCase;

    @InjectMocks
    private CustomerController controller;

    @Test
    @DisplayName("POST /api/user: deve retornar 201 e o body vindo do use case")
    void createUser_shouldReturnCreated() {
        var request = mock(CustomerCreateRequest.class);
        var expected = mock(CustomerResponse.class);

        when(customerUseCase.createUser(request)).thenReturn(expected);

        var response = controller.createUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expected);

        verify(customerUseCase).createUser(request);
        verifyNoMoreInteractions(customerUseCase);
    }
}
