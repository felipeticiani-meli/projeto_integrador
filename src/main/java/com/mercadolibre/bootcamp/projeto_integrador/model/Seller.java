package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sellerId;

    @Column(length = 100)
    private String name;

    @Column(length = 45)
    private String username;

    @Column(length = 60)
    private String email;

    @OneToMany(mappedBy = "seller")
    private List<Product> products;
}
