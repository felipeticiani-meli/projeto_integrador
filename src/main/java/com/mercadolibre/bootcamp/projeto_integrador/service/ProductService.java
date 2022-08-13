package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.EmptyStockException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {
    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IBatchRepository batchRepository;

    @Autowired
    private IManagerService managerService;

    /**
     * Método que retorna os detalhes do produto.
     *
     * @param productId ID do produto
     * @param managerId ID do representante
     * @return Detalhes do produto
     */
    @Override
    public ProductDetailsResponseDto getProductDetails(long productId, long managerId, String orderBy) {
        ensureManagerExists(managerId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("product"));
        List<BatchResponseDto> batches = batchRepository.findAllByProduct(product)
                .stream()
                .filter(batch -> batch.getInboundOrder().getSection().getManager().getManagerId() == managerId)
                .map(BatchResponseDto::new)
                .collect(Collectors.toList());

        if (batches.isEmpty())
            throw new EmptyStockException(product.getProductName());

        return new ProductDetailsResponseDto(productId, sortBatches(batches, orderBy));
    }

    /**
     * Ordena uma lista de Batch de acordo com o parâmetro recebido.
     *
     * @param batches lista de lotes
     * @param orderBy L: batchNumber, Q: currentQuantity, V: dueDate.
     * @return Lista ordenada de batches.
     * @throws BadRequestException Caso o parâmetro seja inválido.
     */
    private List<BatchResponseDto> sortBatches(List<BatchResponseDto> batches, String orderBy) throws BadRequestException {
        if (StringUtils.isBlank(orderBy))
            return batches;

        switch (orderBy.toUpperCase()) {
            case "L":
                return batches.stream()
                        .sorted(Comparator.comparing(BatchResponseDto::getBatchNumber))
                        .collect(Collectors.toList());
            case "Q":
                return batches.stream()
                        .sorted(Comparator.comparing(BatchResponseDto::getCurrentQuantity))
                        .collect(Collectors.toList());
            case "V":
                return batches.stream()
                        .sorted(Comparator.comparing(BatchResponseDto::getDueDate))
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Parâmetro de ordenação inválido. L: batchNumber, Q: currentQuantity, V: dueDate");
        }
    }

    private void ensureManagerExists(long managerId) {
        managerService.findById(managerId);
    }

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
