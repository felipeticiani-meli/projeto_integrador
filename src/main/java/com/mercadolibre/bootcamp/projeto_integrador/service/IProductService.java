package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;

public interface IProductService {
    ProductDetailsResponseDto getProductDetails(long productId, long managerId);
}
