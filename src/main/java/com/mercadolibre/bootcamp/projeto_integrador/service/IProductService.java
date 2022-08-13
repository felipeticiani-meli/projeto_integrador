package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;

import java.util.List;
import java.util.Map;

public interface IProductService {
    Map<Long, Product> getProductMap(List<BatchRequestDto> batchesDto);
    ProductDetailsResponseDto getProductDetails(long productId, long managerId, String orderBy);
}
