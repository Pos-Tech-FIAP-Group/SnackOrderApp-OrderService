package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.CustomerCreateRequest;
import com.fiap.snackapp.core.application.dto.response.CustomerResponse;
import com.fiap.snackapp.core.application.usecases.CustomerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    @PostMapping
    public ResponseEntity<CustomerResponse> createUser(@RequestBody @Valid CustomerCreateRequest customerCreateRequest) {
        CustomerResponse createdProduct = customerUseCase.createUser(customerCreateRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProduct);
    }

}
