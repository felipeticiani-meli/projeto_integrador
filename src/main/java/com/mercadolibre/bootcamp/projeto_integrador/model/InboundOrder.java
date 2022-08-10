package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class InboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderNumber;

    @ManyToOne
    @JoinColumn(name = "sectionCode")
    private Section section;

    private LocalDate orderDate;
}
