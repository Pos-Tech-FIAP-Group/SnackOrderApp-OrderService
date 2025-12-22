package com.fiap.snackapp.core.application.dto.request;

import com.fiap.snackapp.core.application.validation.CPF;


public record CustomerCreateRequest(
    String name,
    String email,
    @CPF
    String cpf
) {}
