package com.fiap.snackapp.adapters.driven.infra.persistence.mapper;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.*;
import com.fiap.snackapp.core.domain.model.*;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceMapper {

    public OrderEntity toEntity(OrderDefinition domain) {
        CustomerEntity customerEntity = null;

        if (domain.getCustomer() != null) {
            customerEntity = new CustomerEntity();
            customerEntity.setId(domain.getCustomer().id());
            customerEntity.setName(domain.getCustomer().name());
            customerEntity.setEmail(domain.getCustomer().email() != null ? domain.getCustomer().email().toString() : null);
            customerEntity.setCpf(domain.getCustomer().cpf().toString());
        }

        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setStatus(domain.getStatus());
        entity.setCustomer(customerEntity);

        Set<OrderItemEntity> itemEntities = domain.getItems().stream()
                .map(item -> {
                    ProductEntity productEntity = new ProductEntity();
                    productEntity.setId(item.productId());

                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setProduct(productEntity);
                    itemEntity.setPrice(item.price());
                    itemEntity.setQuantity(item.quantity());
                    itemEntity.setOrder(entity);

                    Set<AppliedAddOnEntity> addOnEntities = item.appliedAddOns().stream()
                            .map(applied -> {
                                AddOnEntity addOnEntity = new AddOnEntity();
                                addOnEntity.setId(applied.getAddOnDefinition().id());

                                AppliedAddOnEntity appliedEntity = new AppliedAddOnEntity();
                                appliedEntity.setAddOn(addOnEntity);
                                appliedEntity.setOrderItem(itemEntity);
                                appliedEntity.setPrice(applied.getAddOnDefinition().price());
                                appliedEntity.setQuantity(applied.getQuantity());

                                return appliedEntity;
                            })
                            .collect(Collectors.toSet());

                    itemEntity.setAppliedAddOns(addOnEntities);
                    return itemEntity;
                })
                .collect(Collectors.toSet());

        entity.setItems(itemEntities);
        return entity;
    }

    public OrderDefinition toDomain(OrderEntity entity) {
        CustomerDefinition customer = null;

        if (entity.getCustomer() != null) {
            customer = new CustomerDefinition(
                    entity.getCustomer().getId(),
                    entity.getCustomer().getName(),
                    new Email(entity.getCustomer().getEmail()),
                    new CPF(entity.getCustomer().getCpf())
            );
        }

        List<OrderItemDefinition> items = entity.getItems().stream()
                .map(item -> {
                    List<AppliedAddOn> addOns = item.getAppliedAddOns().stream()
                            .map(addOn -> {
                                AddOnDefinition def = new AddOnDefinition(
                                        addOn.getAddOn().getId(),
                                        addOn.getAddOn().getName(),
                                        addOn.getAddOn().getCategory(),
                                        addOn.getPrice()
                                );
                                return new AppliedAddOn(def, addOn.getQuantity());
                            })
                            .toList();

                    return new OrderItemDefinition(
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getProduct().getPrice(),
                            addOns
                    );
                })
                .toList();

        return new OrderDefinition(
                entity.getId(),
                customer,
                entity.getStatus(),
                items,
                entity.getQrCodeUrl(),
                entity.getPaymentId()
        );
    }
}