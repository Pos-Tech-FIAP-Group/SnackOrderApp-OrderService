package com.fiap.snackapp.core.application.mapper;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerDefinition toDomain(CustomerCreateRequest request) {
        return new CustomerDefinition(null, request.name(), new Email(request.email()), new CPF(request.cpf()));
    }

    public CustomerResponse toResponse(CustomerDefinition user) {
        return new CustomerResponse(
                user.id(),
                user.name(),
                user.email().toString(),
                user.cpf().toString()
        );
    }
}
