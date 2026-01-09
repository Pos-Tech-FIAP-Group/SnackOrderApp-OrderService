package com.fiap.snackapp.core.domain.model;

import com.fiap.snackapp.core.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderDefinition {

    private final Long id;
    private final CustomerDefinition customer;
    private OrderStatus status;
    private final List<OrderItemDefinition> items;
    private String qrCodeUrl;
    private String paymentId;

    public OrderDefinition(Long id,
                           CustomerDefinition customer,
                           OrderStatus status,
                           List<OrderItemDefinition> items,
                           String qrCodeUrl,
                           String paymentId) {
        this.id = id;
        this.customer = customer;
        this.status = status;
        this.items = new ArrayList<>(items != null ? items : List.of());
        this.qrCodeUrl = qrCodeUrl;
        this.paymentId = paymentId;
    }

    public void addItem(OrderItemDefinition item) {
        this.items.add(item);
    }
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItemDefinition::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}