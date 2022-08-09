package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.EmptyStockException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {
    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IBatchRepository batchRepository;

    /**
     * Método que retorna os detalhes do produto.
     *
     * @param productId ID do produto
     * @param managerId ID do representante
     * @return Detalhes do produto
     */
    @Override
    public ProductDetailsResponseDto getProductDetails(long productId, long managerId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("product"));
        List<BatchResponseDto> batches = batchRepository.findAllByProduct(product)
                .stream()
                .filter(batch -> batch.getInboundOrder().getSection().getManager().getManagerId() == managerId)
                .map(BatchResponseDto::new)
                .collect(Collectors.toList());

        if (batches.isEmpty())
            throw new EmptyStockException(product.getProductName());

        return new ProductDetailsResponseDto(productId, batches);
    }
}
