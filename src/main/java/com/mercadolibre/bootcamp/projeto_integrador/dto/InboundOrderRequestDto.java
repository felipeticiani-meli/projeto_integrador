package com.mercadolibre.bootcamp.projeto_integrador.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@NoArgsConstructor
public class InboundOrderRequestDto {
    @NotNull(message = "O código do setor não pode estar vazio")
    @Positive(message = "O código do setor deve ser um número positivo")
    private long sectionCode;

    @NotEmpty(message = "A lista de lotes é obrigatória")
    private List<@Valid BatchRequestDto> batchStock;
}
