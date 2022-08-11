package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ProductsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @InjectMocks
    private ProductService service;
    @Mock
    private IBatchRepository batchRepository;
    @Mock
    private IProductRepository productRepository;

    private Product product;
    private List<Batch> batches;

    @BeforeEach
    private void setup() {
        product = ProductsGenerator.newProductFresh();
        batches = BatchGenerator.newBatchList();
    }

    @Test
    void getWarehouses_returnProduct_whenProductsExists() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);

        // Act
        ProductResponseDto foundProduct = service.getWarehouses(product.getProductId());

        // Assert
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertEquals(foundProduct.getWarehouses().get(0).getTotalQuantity(), 50);
        assertEquals(foundProduct.getWarehouses().get(0).getWarehouseCode(), 1);
        assertEquals(foundProduct.getWarehouses().get(1).getTotalQuantity(), 40);
        assertEquals(foundProduct.getWarehouses().get(1).getWarehouseCode(), 2);
    }

    @Test
    void getWarehouses_returnNotFoundException_whenProductNotExist() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getWarehouses(product.getProductId()));

        // Assert
        assertThat(exception.getName()).contains("Product");
        assertThat(exception.getMessage()).contains("There is no product with the specified id");
    }

    @Test
    void getWarehouses_returnProductWithoutWarehouse_whenProductWithoutBatches() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        batches.clear();
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);

        // Act
        ProductResponseDto foundProduct = service.getWarehouses(product.getProductId());

        // Assert
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertThat(foundProduct.getWarehouses()).isEmpty();
    }
}