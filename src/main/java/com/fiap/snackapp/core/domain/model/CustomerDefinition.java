package com.fiap.snackapp.core.domain.model;

import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;

public record CustomerDefinition(
        Long id,
        String name,
        Email email,
        CPF cpf
) {}