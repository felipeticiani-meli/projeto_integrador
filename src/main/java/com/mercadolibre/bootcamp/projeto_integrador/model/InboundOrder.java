package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class InboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderNumber;

    @ManyToOne
    @JoinColumn(name = "sectionCode")
    private Section section;

    @OneToMany
    @JoinColumn(name = "batch_number")
    private List<Batch> batchStock;

    private LocalDate orderDate;
}
