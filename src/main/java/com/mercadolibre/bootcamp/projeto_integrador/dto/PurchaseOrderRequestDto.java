package com.mercadolibre.bootcamp.projeto_integrador.dto;

import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderRequestDto {
    private LocalDate date;
    private long buyerId;
    private boolean orderStatus;
    private List<ProductDto> products;
}
