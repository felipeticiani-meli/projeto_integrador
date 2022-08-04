package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @OneToMany(mappedBy = "batchNuber")
    @JsonIgnoreProperties("product")
    private List<Batch> batches;

    @Column(length = 45)
    private String productName;

    @Column(length = 45)
    private String brand;

    @Column(length = 45)
    private String category;
}
