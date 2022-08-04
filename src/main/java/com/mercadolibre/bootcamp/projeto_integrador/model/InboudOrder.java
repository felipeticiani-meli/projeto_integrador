package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class InboudOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderNumber;

    @ManyToOne
    @JoinColumn(name = "selectionCode")
    private Section section;

    @OneToMany(mappedBy = "inboudOrder")
    @JsonIgnoreProperties("inboudOrder")
    private List<Batch> batcheStock;

    private LocalDate orderDate;
}
