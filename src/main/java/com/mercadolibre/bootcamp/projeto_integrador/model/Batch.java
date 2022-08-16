package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_number")
    @JsonIgnore
    private InboundOrder inboundOrder;

    @Column(precision = 9, scale = 2)
    private BigDecimal productPrice;

    @OneToMany(mappedBy = "batch")
    @JsonIgnore
    private List<BatchPurchaseOrder> batchPurchaseOrders;
}
