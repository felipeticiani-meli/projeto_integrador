package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long batchNumber;

    @ManyToOne
    @JoinColumn(name = "product_id")
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
    private InboundOrder inboundOrder;

    @Column(precision = 9, scale = 2)
    private BigDecimal productPrice;
}
