package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;

public interface CustomerUseCase {
    CustomerResponse createUser(CustomerCreateRequest customerCreateRequest);
}
