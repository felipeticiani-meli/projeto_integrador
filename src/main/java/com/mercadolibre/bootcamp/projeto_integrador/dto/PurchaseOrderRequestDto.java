package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseOrderRequestDto {
    private long buyerId;
    private String orderStatus;
    private List<ProductDto> products;
}
