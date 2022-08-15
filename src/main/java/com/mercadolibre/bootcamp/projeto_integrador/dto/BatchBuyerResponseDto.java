package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.BatchPurchaseOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BatchBuyerResponseDto {
    private long batchNumber;
    private String productName;
    private String brand;
    private Section.Category category;
    private int quantity;
    private LocalDate dueDate;
    private BigDecimal productPrice;

    public BatchBuyerResponseDto(Batch batch) {
        this.batchNumber = batch.getBatchNumber();
        this.productName = batch.getProduct().getProductName();
        this.brand = batch.getProduct().getBrand();
        this.category = batch.getProduct().getCategory();
        this.quantity = batch.getCurrentQuantity();
        this.dueDate = batch.getDueDate();
        this.productPrice = batch.getProductPrice();
    }

    public BatchBuyerResponseDto(BatchPurchaseOrder batchPurchaseOrder) {
        this.batchNumber = batchPurchaseOrder.getBatch().getBatchNumber();
        this.productName = batchPurchaseOrder.getBatch().getProduct().getProductName();
        this.brand = batchPurchaseOrder.getBatch().getProduct().getBrand();
        this.category = batchPurchaseOrder.getBatch().getProduct().getCategory();
        this.quantity = batchPurchaseOrder.getQuantity();
        this.dueDate = batchPurchaseOrder.getBatch().getDueDate();
        this.productPrice = batchPurchaseOrder.getUnitPrice();
    }
}
