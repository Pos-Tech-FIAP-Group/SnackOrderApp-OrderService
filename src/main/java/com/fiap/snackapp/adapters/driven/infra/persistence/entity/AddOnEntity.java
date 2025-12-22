package com.fiap.snackapp.adapters.driven.infra.persistence.entity;

import com.fiap.snackapp.core.domain.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "add_ons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddOnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    public AddOnEntity(String name, Category category, BigDecimal price) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.active = true;
    }
}