package com.fiap.snackapp.core.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "joao@test.com",
            "maria.silva@empresa.com.br",
            "dev_123@tech.io",
            "a@b.cd"
    })
    @DisplayName("Deve criar Email com sucesso quando o formato for válido")
    void shouldCreateValidEmail(String validEmail) {
        var email = new Email(validEmail);

        assertThat(email.value()).isEqualTo(validEmail);
        assertThat(email.toString()).isEqualTo(validEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "email_sem_arroba.com",
            "@sem_usuario.com",
            "sem_dominio@",
            "espacos no@meio.com",
            "test@.com", // ponto logo após arroba
            "test@com"   // sem TLD (.com)
    })
    @DisplayName("Deve lançar exceção quando o formato do email for inválido")
    void shouldThrowExceptionForInvalidEmail(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email inválido");
    }

    @Test
    @DisplayName("Deve permitir email nulo (conforme lógica do construtor)")
    void shouldAllowNullEmail() {
        var email = new Email(null);
        assertThat(email.value()).isNull();
    }

    @Test
    @DisplayName("Deve testar igualdade de Value Objects")
    void shouldTestEquality() {
        var email1 = new Email("teste@teste.com");
        var email2 = new Email("teste@teste.com");
        var email3 = new Email("outro@teste.com");

        assertThat(email1)
                .isEqualTo(email2)     // VOs com mesmo valor são iguais
                .hasSameHashCodeAs(email2)
                .isNotEqualTo(email3);
    }
}
