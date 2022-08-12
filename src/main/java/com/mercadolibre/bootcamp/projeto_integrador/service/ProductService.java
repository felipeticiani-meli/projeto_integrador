package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {
    @Autowired
    private IProductRepository productRepository;

    /**
     * Retorna mapa de produtos por ID
     * @param batchesDto Lotes enviados no pedido de entrada
     * @return Mapa de produtos com identificador como chave
     */
    @Override
    public Map<Long, Product> getProductMap(List<BatchRequestDto> batchesDto) {
        return productRepository
                .findAllById(batchesDto.stream().map(BatchRequestDto::getProductId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));
    }
}
