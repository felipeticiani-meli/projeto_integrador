package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BatchResponseDto {
    private long batchNumber;
    private int currentQuantity;
    private LocalDate dueDate;
    private SectionResponseDto section;

    public BatchResponseDto(Batch batch) {
        setBatchNumber(batch.getBatchNumber());
        setCurrentQuantity(batch.getCurrentQuantity());
        setDueDate(batch.getDueDate());
        setSection(new SectionResponseDto(batch.getInboundOrder().getSection()));
    }
}
