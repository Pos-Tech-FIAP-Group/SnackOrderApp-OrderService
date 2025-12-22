package com.fiap.snackapp.adapters.driven.infra.persistence.entity;

import com.fiap.snackapp.core.domain.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active;

    public ProductEntity(String name, Category category, BigDecimal price, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.active = true;
    }
}