package com.fiap.snackapp.adapters.driven.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_add_ons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppliedAddOnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_id", nullable = false)
    private AddOnEntity addOn;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;
}
