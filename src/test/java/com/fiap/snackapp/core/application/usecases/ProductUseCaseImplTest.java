package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.*;
import com.fiap.snackapp.core.application.dto.response.AddOnResponse;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.application.exception.ResourceNotFoundException;
import com.fiap.snackapp.core.application.mapper.AddOnMapper;
import com.fiap.snackapp.core.application.mapper.ProductMapper;
import com.fiap.snackapp.core.application.repository.AddOnRepositoryPort;
import com.fiap.snackapp.core.application.repository.ProductRepositoryPort;
import com.fiap.snackapp.core.domain.enums.Category;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import com.fiap.snackapp.core.domain.model.DynamicAddOnDecorator;
import com.fiap.snackapp.core.domain.model.Product;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

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

    // --- TESTES DE PRODUTO ---

    @Nested
    @DisplayName("Cenários de Produto")
    class ProductTests {
        @Test
        @DisplayName("Deve criar um produto com sucesso")
        void shouldCreateProductSuccessfully() {
            ProductCreateRequest request = new ProductCreateRequest("X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso");
            ProductDefinition definition = new ProductDefinition(null, "X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso", true);
            ProductDefinition savedDefinition = new ProductDefinition(1L, "X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso", true);
            ProductResponse expectedResponse = new ProductResponse(1L, "X-Burger", Category.LANCHE, BigDecimal.TEN, "Delicioso", true);

            when(productMapper.toProductDefinition(request)).thenReturn(definition);
            when(productRepository.save(definition)).thenReturn(savedDefinition);
            when(productMapper.toProductResponse(savedDefinition)).thenReturn(expectedResponse);

            ProductResponse response = productUseCase.createProduct(request);

            assertNotNull(response);
            assertEquals(1L, response.id());
            verify(productRepository).save(definition);
        }

        @Test
        @DisplayName("Deve buscar produto por ID")
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
        @DisplayName("Deve listar produtos com filtros")
        void shouldGetProductsByFilters() {
            var product = new ProductDefinition(1L, "X", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            when(productRepository.findByFilters(true, Category.LANCHE)).thenReturn(List.of(product));
            when(productMapper.toProductResponse(product)).thenReturn(mock(ProductResponse.class));

            var result = productUseCase.getProductsByFilters(true, Category.LANCHE);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve atualizar produto")
        void shouldUpdateProduct() {
            var request = new ProductUpdateRequest(null, null, BigDecimal.ONE, null, null);
            var updated = new ProductDefinition(1L, "Updated", Category.LANCHE, BigDecimal.ONE, "Desc", true);

            when(productRepository.update(1L, request)).thenReturn(updated);
            when(productMapper.toProductResponse(updated)).thenReturn(mock(ProductResponse.class));

            productUseCase.updateProduct(1L, request);
            verify(productRepository).update(1L, request);
        }

        @Test
        @DisplayName("Deve lançar erro se produto não existir")
        void shouldThrowIfProductNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> productUseCase.getProductById(99L));
        }
    }

    // --- TESTES DE ADICIONAIS (ADD-ONS) ---

    @Nested
    @DisplayName("Cenários de Adicionais")
    class AddOnTests {
        @Test
        @DisplayName("Deve criar adicional")
        void shouldCreateAddOn() {
            var request = new AddOnCreateRequest("Bacon", Category.LANCHE, BigDecimal.TWO);
            var definition = new AddOnDefinition(null, "Bacon", Category.LANCHE, BigDecimal.TWO, true);
            var saved = new AddOnDefinition(10L, "Bacon", Category.LANCHE, BigDecimal.TWO, true);

            when(addOnMapper.toAddOnDefinition(request)).thenReturn(definition);
            when(addOnRepository.save(definition)).thenReturn(saved);
            when(productMapper.toAddOnResponse(saved)).thenReturn(mock(AddOnResponse.class));

            productUseCase.createAddOn(request);
            verify(addOnRepository).save(definition);
        }

        @Test
        @DisplayName("Deve buscar adicional por ID")
        void shouldGetAddOnById() {
            var addOn = new AddOnDefinition(10L, "Bacon", Category.LANCHE, BigDecimal.TWO, true);
            when(addOnRepository.findById(10L)).thenReturn(Optional.of(addOn));
            when(productMapper.toAddOnResponse(addOn)).thenReturn(mock(AddOnResponse.class));

            productUseCase.getAddOnById(10L);
            verify(addOnRepository).findById(10L);
        }

        @Test
        @DisplayName("Deve lançar erro se adicional não existir")
        void shouldThrowIfAddOnNotFound() {
            when(addOnRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> productUseCase.getAddOnById(99L));
        }

        @Test
        @DisplayName("Deve listar adicionais com filtros")
        void shouldListAddOnsByFilters() {
            var addOn = new AddOnDefinition(10L, "Bacon", Category.LANCHE, BigDecimal.TWO, true);
            when(addOnRepository.findByFilters(true, null)).thenReturn(List.of(addOn));
            when(productMapper.toAddOnResponse(addOn)).thenReturn(mock(AddOnResponse.class));

            var result = productUseCase.getAddOnsByFilters(true, null);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve atualizar adicional")
        void shouldUpdateAddOn() {
            var request = new AddOnUpdateRequest(null, null, BigDecimal.TEN, null);
            var updated = new AddOnDefinition(10L, "Bacon", Category.LANCHE, BigDecimal.TEN, true);

            when(addOnRepository.update(10L, request)).thenReturn(updated);
            when(productMapper.toAddOnResponse(updated)).thenReturn(mock(AddOnResponse.class));

            productUseCase.updateAddOn(10L, request);
            verify(addOnRepository).update(10L, request);
        }
    }

    // --- TESTES DE DECORAÇÃO ---

    @Nested
    @DisplayName("Cenários de Decoração de Produto")
    class DecorationTests {
        @Test
        @DisplayName("Deve decorar produto corretamente")
        void shouldDecorateProduct() {
            Long productId = 1L;
            Long addOnId = 10L;

            ProductDefinition base = new ProductDefinition(productId, "Lanche", Category.LANCHE, BigDecimal.valueOf(20), "Base", true);
            AddOnDefinition bacon = new AddOnDefinition(addOnId, "Bacon", Category.LANCHE, BigDecimal.valueOf(2), true);

            var portion = new AddOnPortionRequest(addOnId, 2);
            var request = new ProductCustomizationRequest(productId, List.of(portion));

            when(productRepository.findById(productId)).thenReturn(Optional.of(base));
            when(addOnRepository.findById(addOnId)).thenReturn(Optional.of(bacon));

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            productUseCase.getDecoratedProduct(request);

            verify(productMapper).toDecoratedProductResponse(captor.capture());
            Product decorated = captor.getValue();

            // Valida se é uma instância do Decorator
            assertTrue(decorated instanceof DynamicAddOnDecorator);
            // Valida preço (20 + 2*2 = 24)
            assertEquals(0, BigDecimal.valueOf(24).compareTo(decorated.getPrice()));
        }

        @Test
        @DisplayName("Deve retornar produto sem decoração se lista de addOns for nula")
        void shouldReturnBaseProductIfAddOnsNull() {
            ProductDefinition base = new ProductDefinition(1L, "Base", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            var request = new ProductCustomizationRequest(1L, null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(base));
            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            productUseCase.getDecoratedProduct(request);

            verify(productMapper).toDecoratedProductResponse(captor.capture());
            assertEquals(base, captor.getValue());
        }

        @Test
        @DisplayName("Deve lançar erro se adicional para decoração não existir")
        void shouldThrowIfDecoratorAddOnNotFound() {
            ProductDefinition base = new ProductDefinition(1L, "Base", Category.LANCHE, BigDecimal.TEN, "Desc", true);
            var portion = new AddOnPortionRequest(999L, 1);
            var request = new ProductCustomizationRequest(1L, List.of(portion));

            when(productRepository.findById(1L)).thenReturn(Optional.of(base));
            when(addOnRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> productUseCase.getDecoratedProduct(request));
        }

        @Test
        @DisplayName("Deve lançar erro ao decorar produto inexistente")
        void shouldThrowIfProductToDecorateNotFound() {
            var request = new ProductCustomizationRequest(999L, List.of());
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> productUseCase.getDecoratedProduct(request));
        }
    }
}
