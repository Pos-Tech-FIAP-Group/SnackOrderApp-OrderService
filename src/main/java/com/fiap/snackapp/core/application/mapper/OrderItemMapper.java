package com.fiap.snackapp.core.application.mapper;

import com.fiap.snackapp.core.application.dto.request.ItemRequest;
import com.fiap.snackapp.core.application.dto.response.AddonItemResponse;
import com.fiap.snackapp.core.application.dto.response.OrderItemResponse;
import com.fiap.snackapp.core.domain.model.AppliedAddOn;
import com.fiap.snackapp.core.domain.model.OrderItemDefinition;
import com.fiap.snackapp.core.domain.model.ProductDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    public OrderItemDefinition toDomain(ProductDefinition product, ItemRequest itemRequest, List<AppliedAddOn> appliedAddOns) {
        return new OrderItemDefinition(
                product.getId(),
                product.getName(),
                itemRequest.quantity(),
                product.getPrice(),
                appliedAddOns
        );
    }

    public OrderItemResponse toResponse(OrderItemDefinition item) {
        List<AddonItemResponse> addOns = item.appliedAddOns().stream()
                .map(addOn -> new AddonItemResponse(
                        addOn.getAddOnDefinition().id(),
                        addOn.getAddOnDefinition().name(),
                        addOn.getAddOnDefinition().category(),
                        addOn.getAddOnDefinition().price(),
                        addOn.getQuantity()
                ))
                .toList();

        return new OrderItemResponse(
                item.productId(),
                item.productName(),
                item.price(),
                item.quantity(),
                addOns,
                item.getTotalPrice()
        );
    }
}