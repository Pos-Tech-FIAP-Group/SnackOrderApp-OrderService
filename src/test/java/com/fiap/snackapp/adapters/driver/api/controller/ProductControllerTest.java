package com.fiap.snackapp.adapters.driver.api.controller;

import com.fiap.snackapp.core.application.dto.request.AddOnCreateRequest;
import com.fiap.snackapp.core.application.dto.request.AddOnUpdateRequest;
import com.fiap.snackapp.core.application.dto.request.ProductCreateRequest;
import com.fiap.snackapp.core.application.dto.request.ProductUpdateRequest;
import com.fiap.snackapp.core.application.dto.response.AddOnResponse;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.usecases.ProductUseCase;
import com.fiap.snackapp.core.domain.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductUseCase productUseCase;

    private ProductController controller() {
        return new ProductController(productUseCase);
    }

    @Nested
    @DisplayName("Cenários de Produto")
    class ProductTests {
        @Test
        @DisplayName("GET /api/products/{id}: deve retornar 200 e o produto")
        void getProductById_shouldReturnOk() {
            var product = mock(ProductResponse.class);
            when(productUseCase.getProductById(1L)).thenReturn(product);

            var response = controller().getProductById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(product);
            verify(productUseCase).getProductById(1L);
        }

        @Test
        @DisplayName("GET /api/products: deve retornar lista de produtos com filtros")
        void getProductsByFilters_shouldReturnOk() {
            var product1 = mock(ProductResponse.class);
            var product2 = mock(ProductResponse.class);
            var products = List.of(product1, product2);

            when(productUseCase.getProductsByFilters(true, Category.LANCHE))
                    .thenReturn(products);

            var response = controller().getProductsByFilters(true, Category.LANCHE);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            verify(productUseCase).getProductsByFilters(true, Category.LANCHE);
        }

        @Test
        @DisplayName("POST /api/products: deve retornar 201 e produto criado")
        void createProduct_shouldReturnCreated() {
            var request = mock(ProductCreateRequest.class);
            var created = mock(ProductResponse.class);
            when(productUseCase.createProduct(request)).thenReturn(created);

            var response = controller().createProduct(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(created);
            verify(productUseCase).createProduct(request);
        }

        @Test
        @DisplayName("PATCH /api/products/{id}: deve atualizar e retornar 200")
        void updateProduct_shouldReturnOk() {
            var request = mock(ProductUpdateRequest.class);
            var updated = mock(ProductResponse.class);
            when(productUseCase.updateProduct(1L, request)).thenReturn(updated);

            var response = controller().updateProduct(1L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(updated);
            verify(productUseCase).updateProduct(1L, request);
        }

        @Test
        @DisplayName("DELETE /api/products/{id}: deve desativar produto (active=false)")
        void setProductStatusToFalse_shouldReturnOk() {
            var updated = mock(ProductResponse.class);
            when(productUseCase.updateProduct(eq(10L), any(ProductUpdateRequest.class)))
                    .thenReturn(updated);

            var response = controller().setProductStatusToFalse(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(updated);

            var captor = ArgumentCaptor.forClass(ProductUpdateRequest.class);
            verify(productUseCase).updateProduct(eq(10L), captor.capture());
            assertThat(captor.getValue().active()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cenários de Adicional (AddOn)")
    class AddOnTests {
        @Test
        @DisplayName("GET /api/products/add-ons/{id}: deve retornar 200 e o adicional")
        void getAddOnById_shouldReturnOk() {
            var addOn = mock(AddOnResponse.class);
            when(productUseCase.getAddOnById(5L)).thenReturn(addOn);

            var response = controller().getAddOnById(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(addOn);
            verify(productUseCase).getAddOnById(5L);
        }

        @Test
        @DisplayName("GET /api/products/add-ons: deve retornar lista de adicionais com filtros")
        void getAddOnsByFilters_shouldReturnOk() {
            var addOn1 = mock(AddOnResponse.class);
            var addOn2 = mock(AddOnResponse.class);
            var addOns = List.of(addOn1, addOn2);

            when(productUseCase.getAddOnsByFilters(true, Category.LANCHE))
                    .thenReturn(addOns);

            var response = controller().getAddOnsByFilters(true, Category.LANCHE);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            verify(productUseCase).getAddOnsByFilters(true, Category.LANCHE);
        }

        @Test
        @DisplayName("POST /api/products/add-ons: deve retornar 201 e adicional criado")
        void createAddOn_shouldReturnCreated() {
            var request = mock(AddOnCreateRequest.class);
            var created = mock(AddOnResponse.class);
            when(productUseCase.createAddOn(request)).thenReturn(created);

            var response = controller().createAddOn(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(created);
            verify(productUseCase).createAddOn(request);
        }

        @Test
        @DisplayName("PATCH /api/products/add-ons/{id}: deve atualizar e retornar 200")
        void updateAddOnStatus_shouldReturnOk() {
            var request = mock(AddOnUpdateRequest.class);
            var updated = mock(AddOnResponse.class);
            when(productUseCase.updateAddOn(3L, request)).thenReturn(updated);

            var response = controller().updateAddOnStatus(3L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(updated);
            verify(productUseCase).updateAddOn(3L, request);
        }

        @Test
        @DisplayName("DELETE /api/products/add-ons/{id}: deve desativar adicional (active=false)")
        void setAddOnStatusToFalse_shouldReturnOk() {
            var updated = mock(AddOnResponse.class);
            when(productUseCase.updateAddOn(eq(7L), any(AddOnUpdateRequest.class)))
                    .thenReturn(updated);

            var response = controller().setAddOnStatusToFalse(7L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(updated);

            var captor = ArgumentCaptor.forClass(AddOnUpdateRequest.class);
            verify(productUseCase).updateAddOn(eq(7L), captor.capture());
            assertThat(captor.getValue().active()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cenários de Exception Handler")
    class ExceptionHandlerTests {
        @Test
        @DisplayName("ExceptionHandler: ResourceNotFoundException deve retornar 404")
        void handleResourceNotFound_shouldReturn404() {
            var ex = new ResourceNotFoundException("Produto não encontrado");

            var response = controller().handleResourceNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo("Produto não encontrado");
        }
    }
}
