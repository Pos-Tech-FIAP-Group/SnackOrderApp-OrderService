package com.fiap.snackapp.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicAddOnDecoratorTest {

    @Mock
    private Product decoratedProduct;

    @Mock
    private AddOnDefinition addOn;

    @Mock
    private ProductDefinition productDefinition;

    @Test
    @DisplayName("Deve calcular o preço total corretamente (Base + Adicional * Quantidade)")
    void shouldCalculateTotalPrice() {
        BigDecimal basePrice = new BigDecimal("20.00");
        BigDecimal addOnPrice = new BigDecimal("2.00");
        int quantity = 3;

        when(decoratedProduct.getPrice()).thenReturn(basePrice);
        when(addOn.price()).thenReturn(addOnPrice);

        var decorator = new DynamicAddOnDecorator(decoratedProduct, addOn, quantity);

        assertThat(decorator.getPrice())
                .isEqualByComparingTo(new BigDecimal("26.00"));
    }

    @Test
    @DisplayName("Deve formatar o nome corretamente quando quantidade > 1")
    void shouldFormatNameWithSuffixWhenQuantityGreaterThanOne() {
        when(decoratedProduct.getDescription()).thenReturn("X-Burger");
        when(addOn.name()).thenReturn("Bacon");

        var decorator = new DynamicAddOnDecorator(decoratedProduct, addOn, 2);

        assertThat(decorator.getName()).isEqualTo("X-Burger, com Bacon (x2)");
    }

    @Test
    @DisplayName("Deve formatar o nome corretamente quando quantidade = 1")
    void shouldFormatNameWithoutSuffixWhenQuantityIsOne() {
        when(decoratedProduct.getDescription()).thenReturn("X-Burger");
        when(addOn.name()).thenReturn("Bacon");

        var decorator = new DynamicAddOnDecorator(decoratedProduct, addOn, 1);

        assertThat(decorator.getName()).isEqualTo("X-Burger, com Bacon");
    }

    @Test
    @DisplayName("Deve validar argumentos inválidos no construtor (Quantidade e Nulos)")
    void shouldThrowExceptionForInvalidArguments() {
        // Validação de quantidade zero/negativa
        assertThatThrownBy(() -> new DynamicAddOnDecorator(decoratedProduct, addOn, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade do adicional deve ser positiva.");

        // Validação de nulos (Objects.requireNonNull gerados no construtor)
        assertThatThrownBy(() -> new DynamicAddOnDecorator(null, addOn, 1))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DynamicAddOnDecorator(decoratedProduct, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve adicionar o novo AddOn à lista de aplicados")
    void shouldAddNewAddOnToList() {
        Product.AppliedAddOn existingAddOn = new Product.AppliedAddOn(null, 1);
        when(decoratedProduct.getAppliedAddOns()).thenReturn(List.of(existingAddOn));

        int quantity = 5;
        var decorator = new DynamicAddOnDecorator(decoratedProduct, addOn, quantity);

        List<Product.AppliedAddOn> resultList = decorator.getAppliedAddOns();

        assertThat(resultList).hasSize(2);
        assertThat(resultList.get(0)).isEqualTo(existingAddOn);
        assertThat(resultList.get(1).addOn()).isEqualTo(addOn);
        assertThat(resultList.get(1).quantity()).isEqualTo(quantity);
    }

    @Test
    @DisplayName("Deve delegar métodos simples para o produto decorado")
    void shouldDelegateSimpleMethods() {
        when(decoratedProduct.getId()).thenReturn(123L);
        when(decoratedProduct.getDescription()).thenReturn("Desc");
        when(decoratedProduct.getProductDefinition()).thenReturn(productDefinition);

        var decorator = new DynamicAddOnDecorator(decoratedProduct, addOn, 1);

        assertThat(decorator.getId()).isEqualTo(123L);
        assertThat(decorator.getDescription()).isEqualTo("Desc");
        assertThat(decorator.getProductDefinition()).isEqualTo(productDefinition);
    }

    @Test
    @DisplayName("Deve testar os métodos gerados automaticamente pelo Record (equals, hashCode, toString, accessors)")
    void shouldCoverRecordGeneratedMethods() {
        // Testa os accessors nativos do Record (que não são os da interface Product)
        // O Record gera: decoratedProduct(), addOn(), quantity()
        var decorator = new DynamicAddOnDecorator(decoratedProduct, addOn, 10);

        assertThat(decorator.decoratedProduct()).isEqualTo(decoratedProduct);
        assertThat(decorator.addOn()).isEqualTo(addOn);
        assertThat(decorator.quantity()).isEqualTo(10);

        // Testa equals e hashCode
        var sameDecorator = new DynamicAddOnDecorator(decoratedProduct, addOn, 10);
        var differentDecorator = new DynamicAddOnDecorator(decoratedProduct, addOn, 5);

        assertThat(decorator)
                .isEqualTo(sameDecorator)
                .hasSameHashCodeAs(sameDecorator)
                .isNotEqualTo(differentDecorator)
                .isNotNull()
                .isNotEqualTo(new Object());

        // Testa toString (só para garantir que não quebra e contém o nome da classe)
        assertThat(decorator.toString()).contains("DynamicAddOnDecorator");
    }
}
