package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long purchaseId;
    private LocalDate date;
    private String orderStatus;
    @ManyToOne
    @JoinColumn(name="buyer_id", nullable = false)
    private Buyer buyer;

    @OneToMany(mappedBy = "purchaseId")
    private List<BatchPurchaseOrder> batchPurchaseOrders;

    public PurchaseOrder(PurchaseOrderRequestDto request) {
        this.date = request.getDate();
        this.orderStatus = request.getOrderStatus();
        this.batchPurchaseOrders = getBatchPurchaseOrders();
    }
}
// request.getBuyerId().getBuyerId();