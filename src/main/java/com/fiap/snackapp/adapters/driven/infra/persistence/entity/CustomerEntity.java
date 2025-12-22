package com.fiap.snackapp.adapters.driven.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    @Column(unique = true, nullable = false)
    private String cpf;


    public CustomerEntity(String name, String email, String cpf) {
        this.name = name;
        this.email = email;
        this.cpf = cpf;
    }
}