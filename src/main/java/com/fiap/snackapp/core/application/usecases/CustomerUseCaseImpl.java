package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;
import com.fiap.snackapp.core.application.mapper.CustomerMapper;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerUseCaseImpl implements CustomerUseCase {

    private final CustomerRepositoryPort userRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createUser(CustomerCreateRequest request) {
        CustomerDefinition domainUser = customerMapper.toDomain(request);
        CustomerDefinition savedUser = userRepository.save(domainUser);
        return customerMapper.toResponse(savedUser);
    }
}