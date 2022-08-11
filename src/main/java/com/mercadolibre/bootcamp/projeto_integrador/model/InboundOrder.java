package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderNumber;

    @ManyToOne
    @JoinColumn(name = "sectionCode")
    private Section section;

    private LocalDate orderDate;
}
