package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_number")
    private long batchNumber;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties("batches")
    private Product product;

    private float currentTemperature;

    private float minimumTemperature;

    private int initialQuantity;

    private int currentQuantity;

    private LocalDate manufacturingDate;

    private LocalDateTime manufacturingTime;

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "order_number")
    @JsonIgnoreProperties("batches")
    private InboundOrder inboundOrder;
}
