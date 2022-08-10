package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @Column(length = 45)
    private String productName;

    @Column(length = 45)
    private String brand;

    @Column(columnDefinition = Section.Category.mysqlDefinition)
    @Enumerated(EnumType.STRING)
    private Section.Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;
}
