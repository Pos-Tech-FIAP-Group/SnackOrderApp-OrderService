package com.fiap.snackapp.core.application.dto.response;

public record CustomerResponse(

    Long id,
    String name,
    String email,
    String cpf
) {}
