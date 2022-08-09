package com.mercadolibre.bootcamp.projeto_integrador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailsResponseDto {
    private long productId;
    private List<BatchResponseDto> batchStock;
}
