package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BatchDueDateResponseDto {
    private long batchNumber;
    private long productId;
    private String productName;
    private Section.Category productCategory;
    private LocalDate dueDate;
    private int currentQuantity;

    public BatchDueDateResponseDto(Batch batch) {
        setBatchNumber(batch.getBatchNumber());
        setProductId(batch.getProduct().getProductId());
        setProductName(batch.getProduct().getProductName());
        setProductCategory(batch.getProduct().getCategory());
        setDueDate(batch.getDueDate());
        setCurrentQuantity(batch.getCurrentQuantity());
    }
}
