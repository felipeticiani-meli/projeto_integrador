package com.mercadolibre.bootcamp.projeto_integrador.dto;

import javax.validation.Valid;
import javax.validation.constraints.*;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseOrderRequestDto {
    @NotNull(message = "O status da compra não pode estar vazio")
    @Pattern(regexp = "^(Closed|Opened)$", message = "Status só pode ser Opened ou Closed")
    private String orderStatus;

    @NotNull(message = "Objeto batch é obrigatório")
    private @Valid BatchPurchaseOrderRequestDto batch;
}
