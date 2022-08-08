package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @Column(length = 45)
    private String productName;

    @Column(length = 45)
    private String brand;

    @Column(length = 45)
    private String category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;
}
