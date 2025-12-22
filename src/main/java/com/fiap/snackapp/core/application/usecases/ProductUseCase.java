package com.fiap.snackapp.core.application.usecases;

import com.fiap.snackapp.core.application.dto.request.*;
import com.fiap.snackapp.core.application.dto.response.AddOnResponse;
import com.fiap.snackapp.core.application.dto.response.DecoratedProductResponse;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.domain.enums.Category;

import java.util.List;

public interface ProductUseCase {
    ProductResponse createProduct(ProductCreateRequest createRequest);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getProductsByFilters(Boolean active, Category category);
    ProductResponse updateProduct(Long id, ProductUpdateRequest updateRequest);

    AddOnResponse createAddOn(AddOnCreateRequest createRequest);
    AddOnResponse getAddOnById(Long id);
    List<AddOnResponse> getAddOnsByFilters(Boolean active, Category category);
    AddOnResponse updateAddOn(Long id, AddOnUpdateRequest addOnUpdateRequest);

    DecoratedProductResponse getDecoratedProduct(ProductCustomizationRequest customizationRequest);
}