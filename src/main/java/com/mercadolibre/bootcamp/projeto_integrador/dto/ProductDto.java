package com.mercadolibre.bootcamp.projeto_integrador.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDto {
    private long productId;
    private int quantity;
}
