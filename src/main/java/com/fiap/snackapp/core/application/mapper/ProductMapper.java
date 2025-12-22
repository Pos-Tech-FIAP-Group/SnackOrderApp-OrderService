package com.fiap.snackapp.core.application.mapper;

import com.fiap.snackapp.core.application.dto.request.ProductCreateRequest;
import com.fiap.snackapp.core.application.dto.response.AddOnResponse;
import com.fiap.snackapp.core.application.dto.response.AppliedAddOnResponse;
import com.fiap.snackapp.core.application.dto.response.DecoratedProductResponse;
import com.fiap.snackapp.core.application.dto.response.ProductResponse;
import com.fiap.snackapp.core.domain.model.AddOnDefinition;
import com.fiap.snackapp.core.domain.model.Product;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(ProductDefinition product) {
        if (product == null) return null;
        return new ProductResponse(product.id(), product.name(), product.category(), product.price(), product.description(), product.active());
    }

    public AddOnResponse toAddOnResponse(AddOnDefinition addOn) {
        if (addOn == null) return null;
        return new AddOnResponse(addOn.id(), addOn.name(), addOn.category(), addOn.price(), addOn.active());
    }

    public DecoratedProductResponse toDecoratedProductResponse(Product product) {
        if (product == null) return null;
        ProductDefinition baseDef = product.getProductDefinition();
        ProductResponse productViewDTO = toProductResponse(baseDef);

        List<AppliedAddOnResponse> appliedAddOnDTOs = product.getAppliedAddOns().stream()
                .map(appliedAddOn -> new AppliedAddOnResponse(
                        appliedAddOn.addOn().id(),
                        appliedAddOn.addOn().name(),
                        appliedAddOn.addOn().category(),
                        appliedAddOn.addOn().price(),
                        appliedAddOn.quantity(),
                        appliedAddOn.addOn().price().multiply(BigDecimal.valueOf(appliedAddOn.quantity()))
                ))
                .toList();

        return new DecoratedProductResponse(
                product.getId(),
                product.getDescription(),
                product.getPrice(),
                productViewDTO,
                appliedAddOnDTOs
        );
    }

    public ProductDefinition toProductDefinition(ProductCreateRequest dto) {
        if (dto == null) return null;
        return new ProductDefinition(dto.name(), dto.category(), dto.price(), dto.description());
    }
}