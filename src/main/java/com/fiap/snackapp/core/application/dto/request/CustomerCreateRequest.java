package com.fiap.snackapp.core.application.dto.request;


import org.hibernate.validator.constraints.br.CPF;

public record CustomerCreateRequest(
    String name,
    String email,
    @CPF
    String cpf
) {}
