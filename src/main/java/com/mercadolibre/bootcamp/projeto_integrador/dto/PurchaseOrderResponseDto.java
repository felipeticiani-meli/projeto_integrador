package com.mercadolibre.bootcamp.projeto_integrador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponseDto {
    private long purchaseOrderId;
    private BigDecimal totalPrice;
}
