package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;

import java.util.List;

public interface IProductMapService {
    ProductMapService.ProductMap getProductMap(List<BatchRequestDto> batchesDto);
}
