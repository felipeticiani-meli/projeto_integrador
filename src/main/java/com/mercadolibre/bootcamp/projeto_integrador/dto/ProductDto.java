package com.mercadolibre.bootcamp.projeto_integrador.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    @NotNull(message = "O id do produto não pode estar vazio")
    @Positive(message = "O id do produto deve ser um número positivo")
    private long productId;

    @NotNull(message = "A quantidade do produto deve ser informada")
    @Min(value = 0, message = "A quantidade do produto deve ser maior ou igual a 0")
    private int quantity;
}
