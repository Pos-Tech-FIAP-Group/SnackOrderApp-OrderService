package com.fiap.snackapp.adapters.driven.infra.persistence.entity;

import com.fiap.snackapp.core.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String qrCodeUrl ;

    private String paymentId ;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItemEntity> items = new HashSet<>();
}
