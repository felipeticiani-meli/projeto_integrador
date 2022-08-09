package com.mercadolibre.bootcamp.projeto_integrador.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseOrderRequestDto {
    private LocalDate date;
    private long buyerId;
    private boolean isFinished;
    private List<ProductDto> products;
}
