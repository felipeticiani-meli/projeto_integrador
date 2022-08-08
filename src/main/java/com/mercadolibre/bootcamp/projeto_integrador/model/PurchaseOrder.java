package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long purchaseId;
    private LocalDate date;
    private boolean orderStatus;
    @ManyToOne
    @JoinColumn(name="buyer_id", nullable = false)
    private Buyer buyerId;

    @OneToMany(mappedBy = "purchaseId")
    private List<BatchPurchaseOrder> batchPurchaseOrders;

}
