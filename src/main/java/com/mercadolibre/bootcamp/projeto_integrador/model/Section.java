package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long selectionCode;

    @ManyToOne
    @JoinColumn(name = "warehouse_code")
    @JsonIgnoreProperties("sections")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "managerId")
    @JsonIgnoreProperties("sections")
    private Manager manager;

    @Column(name = "category", columnDefinition = "enum('fresh', 'chilled', 'frozen')")
    private String category;

    private int maxBatches;
}
