package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.AddOnPortionRequest;
import com.fiap.snackapp.core.application.dto.request.ProductCreateRequest;
import com.fiap.snackapp.core.application.dto.request.ProductCustomizationRequest;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.mapper.AddOnMapper;
import com.fiap.snackapp.core.application.mapper.ProductMapper;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import com.fiap.snackapp.core.domain.model.Product;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private AddOnRepositoryPort addOnRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private AddOnMapper addOnMapper;

    @InjectMocks
    private ProductUseCaseImpl productUseCase;

    @Test
    @DisplayName("Deve criar um produto com sucesso")
    void shouldCreateProductSuccessfully() {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest("X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso");
        ProductDefinition definition = new ProductDefinition(null, "X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso", true);
        ProductDefinition savedDefinition = new ProductDefinition(1L, "X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso", true);
        ProductResponse expectedResponse = new ProductResponse(1L, "X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso", true);

        when(productMapper.toProductDefinition(request)).thenReturn(definition);
        when(productRepository.save(definition)).thenReturn(savedDefinition);
        when(productMapper.toProductResponse(savedDefinition)).thenReturn(expectedResponse);

        // Act
        ProductResponse response = productUseCase.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("X-Burger", response.name());
        verify(productRepository).save(definition);
    }

    @Test
    @DisplayName("Deve buscar produto por ID existente")
    void shouldGetProductById() {
        Long id = 1L;
        ProductDefinition product = new ProductDefinition(id, "Coke", Category.BEBIDA, BigDecimal.valueOf(5), "Cold", true);
        ProductResponse expected = new ProductResponse(id, "Coke", Category.BEBIDA, BigDecimal.valueOf(5), "Cold", true);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(expected);

        ProductResponse response = productUseCase.getProductById(id);

        assertEquals(expected, response);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto inexistente")
    void shouldThrowWhenProductNotFound() {
        Long id = 99L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productUseCase.getProductById(id));
    }

    @Test
    @DisplayName("Deve calcular preço de produto decorado com adicionais")
    void shouldCalculateDecoratedProductPrice() {
        // Arrange
        Long productId = 1L;
        Long addOnId = 10L;

        ProductDefinition baseProduct = new ProductDefinition(productId, "Lanche", Category.LANCHE, BigDecimal.valueOf(20), "Base", true);
        AddOnDefinition bacon = new AddOnDefinition(addOnId, "Bacon", Category.LANCHE, BigDecimal.valueOf(2), true);

        var portion = new AddOnPortionRequest(addOnId, 2);
        var request = new ProductCustomizationRequest(productId, List.of(portion));

        when(productRepository.findById(productId)).thenReturn(Optional.of(baseProduct));
        when(addOnRepository.findById(addOnId)).thenReturn(Optional.of(bacon));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        // Act
        productUseCase.getDecoratedProduct(request);

        // Assert
        verify(productMapper).toDecoratedProductResponse(productCaptor.capture());

        Product decoratedResult = productCaptor.getValue();

        // Validação da lógica (20 + 2*2 = 24)
        assertEquals(0, BigDecimal.valueOf(24).compareTo(decoratedResult.getPrice()));
    }



    @Test
    @DisplayName("Deve lançar erro ao tentar decorar com produto inexistente")
    void shouldThrowErrorDecoratingUnknownProduct() {
        var request = new ProductCustomizationRequest(999L, List.of());
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productUseCase.getDecoratedProduct(request));
    }
}
