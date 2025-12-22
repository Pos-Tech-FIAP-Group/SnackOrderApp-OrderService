package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.*;
import com.fiap.snackapp.core.application.dto.response.AddOnResponse;
import com.fiap.snackapp.core.application.dto.response.DecoratedProductResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductUseCaseImpl implements ProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final AddOnRepositoryPort addOnRepository;
    private final ProductMapper productMapper;
    private final AddOnMapper addOnMapper;

    @Override
    public ProductResponse createProduct(ProductCreateRequest createRequest) {
        ProductDefinition domainProduct = productMapper.toProductDefinition(createRequest);
        ProductDefinition savedProduct = productRepository.save(domainProduct);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
    }

    @Override
    public List<ProductResponse> getProductsByFilters(Boolean active, Category category) {
        return productRepository.findByFilters(active, category).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest productUpdateRequest) {
        return productMapper.toProductResponse(productRepository.update(id, productUpdateRequest));
    }

    @Override
    public AddOnResponse createAddOn(AddOnCreateRequest createRequest) {
        AddOnDefinition domainAddOn = addOnMapper.toAddOnDefinition(createRequest);
        AddOnDefinition savedAddOn = addOnRepository.save(domainAddOn);
        return productMapper.toAddOnResponse(savedAddOn);
    }

    @Override
    public AddOnResponse getAddOnById(Long id) {
        return addOnRepository.findById(id)
                .map(productMapper::toAddOnResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Adicional não encontrado com id: " + id));
    }

    @Override
    public List<AddOnResponse> getAddOnsByFilters(Boolean active, Category category) {
         return addOnRepository.findByFilters(active, category).stream()
                 .map(productMapper::toAddOnResponse)
                 .toList();
    }

    @Override
    public AddOnResponse updateAddOn(Long id, AddOnUpdateRequest addOnUpdateRequest) {
        return productMapper.toAddOnResponse(addOnRepository.update(id, addOnUpdateRequest));
    }

    @Override
    public DecoratedProductResponse getDecoratedProduct(ProductCustomizationRequest customizationRequest) {
        Product currentProductState = productRepository.findById(customizationRequest.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + customizationRequest.productId()));

        if (customizationRequest.addOnPortions() != null) {
            for (var portion : customizationRequest.addOnPortions()) {
                AddOnDefinition addOn = addOnRepository.findById(portion.addOnId())
                        .orElseThrow(() -> new ResourceNotFoundException("Adicional não encontrado com id: " + portion.addOnId()));
                currentProductState = new DynamicAddOnDecorator(currentProductState, addOn, portion.quantity());
            }
        }
        return productMapper.toDecoratedProductResponse(currentProductState);
    }
}