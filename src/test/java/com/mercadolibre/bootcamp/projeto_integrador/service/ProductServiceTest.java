package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.EmptyStockException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ManagerGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ProductsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IBatchRepository batchRepository;

    @Mock
    private IManagerRepository managerRepository;

    private Product product;
    private List<Batch> batches;

    @BeforeEach
    private void setup() {
        product = ProductsGenerator.newProductFresh();
        batches = BatchGenerator.newBatchList();
    }

    @Test
    void getProductDetails_returnProductWithBatches_whenValidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(2)));

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

    @Test
    void getProductDetails_returnNotFoundException_whenInvalidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(2)));

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.getProductDetails(product.getProductId(), 2, null));

        // Assert
        assertThat(exception.getMessage()).isEqualTo("There is no product with the specified id");
        verify(batchRepository, never()).findAllByProduct(ArgumentMatchers.any());
    }

    @Test
    void getProductDetails_returnEmptyStockException_whenProductWithoutBatchStock() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(1)));

        // Act
        EmptyStockException exception = assertThrows(EmptyStockException.class,
                () -> productService.getProductDetails(product.getProductId(), 1, null));

        // Assert
        assertThat(exception.getMessage()).contains("doesn't have stock");
        assertThat(exception.getMessage()).contains(product.getProductName());
    }

    @Test
    void getProductDetails_returnOrderedByBatchNumber_whenValidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(2)));

        long maxBatchNumber = batches.stream()
                .max(Comparator.comparing(Batch::getBatchNumber))
                .get().getBatchNumber();
        long minBatchNumber = batches.stream()
                .min(Comparator.comparing(Batch::getBatchNumber))
                .get().getBatchNumber();

        // Act
        ProductDetailsResponseDto foundProduct = productService.getProductDetails(product.getProductId(), 2, "l");

        // Assert
        assertThat(foundProduct.getProductId()).isNotNull();
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertEquals(foundProduct.getBatchStock().size(), batches.size());
        assertEquals(foundProduct.getBatchStock().get(0).getBatchNumber(), minBatchNumber);
        assertEquals(foundProduct.getBatchStock().get(foundProduct.getBatchStock().size() - 1).getBatchNumber(), maxBatchNumber);
    }

    @Test
    void getProductDetails_returnOrderedByCurrentQuantity_whenValidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(2)));

        long maxQuantity = batches.stream()
                .max(Comparator.comparing(Batch::getCurrentQuantity))
                .get().getCurrentQuantity();
        long minQuantity = batches.stream()
                .min(Comparator.comparing(Batch::getCurrentQuantity))
                .get().getCurrentQuantity();

        // Act
        ProductDetailsResponseDto foundProduct = productService.getProductDetails(product.getProductId(), 2, "q");

        // Assert
        assertThat(foundProduct.getProductId()).isNotNull();
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertEquals(foundProduct.getBatchStock().size(), batches.size());
        assertEquals(foundProduct.getBatchStock().get(0).getCurrentQuantity(), minQuantity);
        assertEquals(foundProduct.getBatchStock().get(foundProduct.getBatchStock().size() - 1).getCurrentQuantity(), maxQuantity);
    }

    @Test
    void getProductDetails_returnOrderedByDueDate_whenValidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(2)));

        LocalDate maxDueDate = batches.stream()
                .max(Comparator.comparing(Batch::getDueDate))
                .get().getDueDate();
        LocalDate minDueDate = batches.stream()
                .min(Comparator.comparing(Batch::getDueDate))
                .get().getDueDate();

        // Act
        ProductDetailsResponseDto foundProduct = productService.getProductDetails(product.getProductId(), 2, "v");

        // Assert
        assertThat(foundProduct.getProductId()).isNotNull();
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertEquals(foundProduct.getBatchStock().size(), batches.size());
        assertEquals(foundProduct.getBatchStock().get(0).getDueDate(), minDueDate);
        assertEquals(foundProduct.getBatchStock().get(foundProduct.getBatchStock().size() - 1).getDueDate(), maxDueDate);
    }

    @Test
    void getProductDetails_returnBadRequestException_whenInvalidOrderParameter() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(ManagerGenerator.getManagerWithId(2)));

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> productService.getProductDetails(product.getProductId(), 2, "AB"));

        // Assert
        assertThat(exception.getMessage()).contains("Parâmetro de ordenação inválido");
    }
}