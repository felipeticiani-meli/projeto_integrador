package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.Valid;
import javax.validation.constraints.*;

import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseOrderRequestDto {
    @NotNull(message = "O id do cliente não pode estar vazio")
    @Positive(message = "O id do cliente deve ser um número positivo")
    private long buyerId;

    @NotNull(message = "O status da compra não pode estar vazio")
    @Pattern(regexp = "^(Closed|Opened)$", message = "Status só pode ser Opened ou Closed")
    private String orderStatus;

    @NotEmpty(message = "A lista de produtos é obrigatória")
    private List<@Valid ProductDto> products;
}
