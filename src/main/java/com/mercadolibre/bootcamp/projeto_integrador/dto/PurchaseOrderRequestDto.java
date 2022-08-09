package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderRequestDto {

    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "A data da inserção no carrinho deve ser informada")
    @PastOrPresent(message = "A data da inserção no carrinho deve ser menor ou igual a data atual")
    private LocalDate date;

    @NotNull(message = "O id do cliente não pode estar vazio")
    @Positive(message = "O id do cliente deve ser um número positivo")
    private long buyerId;

    private boolean orderStatus;

    @NotEmpty(message = "A lista de produtos é obrigatória")
    private List<ProductDto> products;
}
