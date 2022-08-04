package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long warehouseCode;

    @Column(length = 50)
    private String location;
}
