package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Data
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long purchaseId;
    private LocalDate date;
    private boolean orderStatus;
    private Buyer buyerId;
    private BatchPurchaseOrder purchaseOrderBatch;
}
