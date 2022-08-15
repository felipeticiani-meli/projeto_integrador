package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.EmptyStockException;
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
    private ProductService service;
    @Mock
    private IBatchRepository batchRepository;
    @Mock
    private IProductRepository productRepository;
    @Mock
    private IManagerService managerService;

    private Product product;
    private List<Batch> batches;
    private Manager manager;

    @BeforeEach
    private void setup() {
        product = ProductsGenerator.newProductFresh();
        batches = BatchGenerator.newBatchList();
        manager = batches.get(0).getInboundOrder().getSection().getManager();
    }

    @Test
    void getWarehouses_returnProduct_whenProductsExists() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(manager);

        // Act
        ProductResponseDto foundProduct = service.getWarehouses(product.getProductId(), manager.getManagerId());

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
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(manager);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getWarehouses(product.getProductId(), manager.getManagerId()));

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
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(manager);

        // Act
        ProductResponseDto foundProduct = service.getWarehouses(product.getProductId(), manager.getManagerId());

        // Assert
        assertEquals(foundProduct.getProductId(), product.getProductId());
        assertThat(foundProduct.getWarehouses()).isEmpty();
    }

    @Test
    void getProductDetails_returnProductWithBatches_whenValidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(ManagerGenerator.getManagerWithId(2));

        // Act
        ProductDetailsResponseDto foundProduct = service.getProductDetails(product.getProductId(), 2, null);

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
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(ManagerGenerator.getManagerWithId(2));

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getProductDetails(product.getProductId(), 2, null));

        // Assert
        assertThat(exception.getMessage()).isEqualTo("There is no product with the specified id");
        verify(batchRepository, never()).findAllByProduct(ArgumentMatchers.any());
    }

    @Test
    void getProductDetails_returnEmptyStockException_whenProductWithoutBatchStock() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(ManagerGenerator.getManagerWithId(1));

        // Act
        EmptyStockException exception = assertThrows(EmptyStockException.class,
                () -> service.getProductDetails(product.getProductId(), 1, null));

        // Assert
        assertThat(exception.getMessage()).contains("doesn't have stock");
        assertThat(exception.getMessage()).contains(product.getProductName());
    }

    @Test
    void getProductDetails_returnOrderedByBatchNumber_whenValidProduct() {
        // Arrange
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(ManagerGenerator.getManagerWithId(2));

        long maxBatchNumber = batches.stream()
                .max(Comparator.comparing(Batch::getBatchNumber))
                .get().getBatchNumber();
        long minBatchNumber = batches.stream()
                .min(Comparator.comparing(Batch::getBatchNumber))
                .get().getBatchNumber();

        // Act
        ProductDetailsResponseDto foundProduct = service.getProductDetails(product.getProductId(), 2, "l");

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
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(ManagerGenerator.getManagerWithId(2));

        long maxQuantity = batches.stream()
                .max(Comparator.comparing(Batch::getCurrentQuantity))
                .get().getCurrentQuantity();
        long minQuantity = batches.stream()
                .min(Comparator.comparing(Batch::getCurrentQuantity))
                .get().getCurrentQuantity();

        // Act
        ProductDetailsResponseDto foundProduct = service.getProductDetails(product.getProductId(), 2, "q");

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
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(ManagerGenerator.getManagerWithId(2));

        LocalDate maxDueDate = batches.stream()
                .max(Comparator.comparing(Batch::getDueDate))
                .get().getDueDate();
        LocalDate minDueDate = batches.stream()
                .min(Comparator.comparing(Batch::getDueDate))
                .get().getDueDate();

        // Act
        ProductDetailsResponseDto foundProduct = service.getProductDetails(product.getProductId(), 2, "v");

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
        when(managerService.findById(ArgumentMatchers.anyLong())).thenReturn(manager);
        when(productRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(product));
        when(batchRepository.findAllByProduct(ArgumentMatchers.any())).thenReturn(batches);

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.getProductDetails(product.getProductId(), manager.getManagerId(), "AB"));

        // Assert
        assertThat(exception.getMessage()).contains("Parâmetro de ordenação inválido");
    }
}