package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class BatchPurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long batchPurchaseId;
    @ManyToOne
    private PurchaseOrder purchaseId;
    @ManyToOne
    private Batch batchNumber;
    @Column(precision = 9, scale = 2)
    private BigDecimal unitPrice;
    private int quantity;
}
