package com.fiap.snackapp.core.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CPFTest {

    @Test
    @DisplayName("Deve criar CPF válido com 11 dígitos numéricos")
    void shouldCreateValidCPF() {
        String validValue = "12345678901";
        var cpf = new CPF(validValue);

        assertThat(cpf.value()).isEqualTo(validValue);
        assertThat(cpf.toString()).isEqualTo(validValue);
    }

    @ParameterizedTest
    @NullSource // Testa passando null
    @ValueSource(strings = {
            "",             // Vazio
            "123",          // Curto
            "123456789012", // Longo (12 dígitos)
            "1234567890a",  // Com letra
            "123.456.789-00", // Com formatação (sua regex \d{11} só aceita números puros)
            "           "   // Espaços em branco
    })
    @DisplayName("Deve lançar exceção para CPF inválido (nulo, tamanho errado ou não numérico)")
    void shouldThrowExceptionForInvalidCPF(String invalidValue) {
        assertThatThrownBy(() -> new CPF(invalidValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    @DisplayName("Deve validar igualdade entre VOs de CPF")
    void shouldTestEquality() {
        var cpf1 = new CPF("11122233344");
        var cpf2 = new CPF("11122233344");
        var cpf3 = new CPF("99988877766");

        assertThat(cpf1)
                .isEqualTo(cpf2)
                .hasSameHashCodeAs(cpf2)
                .isNotEqualTo(cpf3);
    }
}
