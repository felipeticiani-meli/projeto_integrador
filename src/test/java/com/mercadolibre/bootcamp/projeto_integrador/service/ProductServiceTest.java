package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ProductsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IBatchRepository batchRepository;

    @Test
    void getProductDetails_returnProductWithBatches_whenValidProduct() {
        // Arrange
        Product product = ProductsGenerator.newProductFresh();
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        List<Batch> batches = BatchGenerator.newBatchList();
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);

        // Act
        ProductDetailsResponseDto foundProduct = productService.getProductDetails(product.getProductId(), 2, null);

        // Assert
        assertThat(foundProduct.getProductId()).isNotNull();
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertEquals(foundProduct.getBatchStock().size(), batches.size());
        assertEquals(foundProduct.getBatchStock().get(0).getBatchNumber(), batches.get(0).getBatchNumber());
        assertEquals(foundProduct.getBatchStock().get(1).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(foundProduct.getBatchStock().get(2).getBatchNumber(), batches.get(2).getBatchNumber());
    }
}