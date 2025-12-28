package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.ProductCreateRequest;
import com.fiap.snackapp.core.application.dto.request.ProductUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.usecases.ProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductUseCase productUseCase;

    private ProductController controller() {
        return new ProductController(productUseCase);
    }

    @Test
    @DisplayName("GET /api/products/{id}: deve retornar 200 e o produto do use case")
    void getProductById_shouldReturnOk() {
        var product = mock(ProductResponse.class);
        when(productUseCase.getProductById(1L)).thenReturn(product);

        var response = controller().getProductById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(product);
        verify(productUseCase).getProductById(1L);
        verifyNoMoreInteractions(productUseCase);
    }

    @Test
    @DisplayName("POST /api/products: deve retornar 201 e o produto criado")
    void createProduct_shouldReturnCreated() {
        var request = mock(ProductCreateRequest.class);
        var created = mock(ProductResponse.class);
        when(productUseCase.createProduct(request)).thenReturn(created);

        var response = controller().createProduct(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
        verify(productUseCase).createProduct(request);
        verifyNoMoreInteractions(productUseCase);
    }

    @Test
    @DisplayName("DELETE /api/products/{id}: deve chamar updateProduct com active=false e retornar 200")
    void deleteProduct_shouldSetActiveFalse() {
        var updated = mock(ProductResponse.class);
        when(productUseCase.updateProduct(eq(10L), any(ProductUpdateRequest.class))).thenReturn(updated);

        var response = controller().setProductStatusToFalse(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);

        var captor = ArgumentCaptor.forClass(ProductUpdateRequest.class);
        verify(productUseCase).updateProduct(eq(10L), captor.capture());
        assertThat(captor.getValue().active()).isFalse();

        verifyNoMoreInteractions(productUseCase);
    }

    @Test
    @DisplayName("ExceptionHandler: ResourceNotFoundException deve virar 404 com a mensagem")
    void handleResourceNotFound_shouldReturn404() {
        var ex = new ResourceNotFoundException("Produto não encontrado");

        var response = controller().handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Produto não encontrado");
    }
}
