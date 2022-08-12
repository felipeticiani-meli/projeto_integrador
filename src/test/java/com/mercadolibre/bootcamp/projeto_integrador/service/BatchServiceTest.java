package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @InjectMocks
    private BatchService service;

    @Mock
    private IBatchRepository batchRepository;

    private List<Batch> batches;

    @BeforeEach
    private void setup() {
        batches = BatchGenerator.newBatchList();
    }

    @Test
    void findAll_returnBatches_whenBatchesExists() {
        // Arrange
        when(batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfter(ArgumentMatchers.anyInt(), ArgumentMatchers.any()))
                .thenReturn(batches);
        // Act
        List<BatchBuyerResponseDto> foundBatches = service.findAll();

        // Assert
        assertThat(foundBatches).isNotEmpty();
        assertEquals(foundBatches.size(), batches.size());
        assertEquals(foundBatches.get(0).getBatchNumber(), batches.get(0).getBatchNumber());
        assertEquals(foundBatches.get(1).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(foundBatches.get(2).getBatchNumber(), batches.get(2).getBatchNumber());
    }

    @Test
    void findAll_returnNotFoundException_whenBatchesNotExists() {
        // Arrange
        batches.clear();
        when(batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfter(ArgumentMatchers.anyInt(), ArgumentMatchers.any()))
                .thenReturn(batches);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findAll());

        // Assert
        assertThat(exception.getName()).contains("Products");
        assertEquals(exception.getMessage(), "There are no products in stock");
    }

    @Test
    void findBatchByCategory_returnBatchesChilled_whenValidCategory() {
        // Arrange
        batches = BatchGenerator.newBatchListChilled();
        when(batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(batches);

        // Act
        List<BatchBuyerResponseDto> foundBatches = service.findBatchByCategory("RF");

        // Assert
        assertThat(foundBatches).isNotEmpty();
        assertEquals(foundBatches.size(), batches.size());
        assertEquals(foundBatches.get(0).getCategory(), Section.Category.CHILLED);
        assertEquals(foundBatches.get(1).getCategory(), Section.Category.CHILLED);
        assertEquals(foundBatches.get(2).getCategory(), Section.Category.CHILLED);
        assertEquals(foundBatches.get(0).getBatchNumber(), batches.get(0).getBatchNumber());
        assertEquals(foundBatches.get(1).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(foundBatches.get(2).getBatchNumber(), batches.get(2).getBatchNumber());
    }

    @Test
    void findBatchByCategory_returnBatchesFresh_whenValidCategory() {
        // Arrange
        when(batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(batches);

        // Act
        List<BatchBuyerResponseDto> foundBatches = service.findBatchByCategory("fs");

        // Assert
        assertThat(foundBatches).isNotEmpty();
        assertEquals(foundBatches.size(), batches.size());
        assertEquals(foundBatches.get(0).getCategory(), Section.Category.FRESH);
        assertEquals(foundBatches.get(1).getCategory(), Section.Category.FRESH);
        assertEquals(foundBatches.get(2).getCategory(), Section.Category.FRESH);
        assertEquals(foundBatches.get(0).getBatchNumber(), batches.get(0).getBatchNumber());
        assertEquals(foundBatches.get(1).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(foundBatches.get(2).getBatchNumber(), batches.get(2).getBatchNumber());
    }

    @Test
    void findBatchByCategory_returnBatchesFrozen_whenValidCategory() {
        // Arrange
        batches = BatchGenerator.newBatchListFrozen();
        when(batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(batches);

        // Act
        List<BatchBuyerResponseDto> foundBatches = service.findBatchByCategory("FF");

        // Assert
        assertThat(foundBatches).isNotEmpty();
        assertEquals(foundBatches.size(), batches.size());
        assertEquals(foundBatches.get(0).getCategory(), Section.Category.FROZEN);
        assertEquals(foundBatches.get(1).getCategory(), Section.Category.FROZEN);
        assertEquals(foundBatches.get(2).getCategory(), Section.Category.FROZEN);
        assertEquals(foundBatches.get(0).getBatchNumber(), batches.get(0).getBatchNumber());
        assertEquals(foundBatches.get(1).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(foundBatches.get(2).getBatchNumber(), batches.get(2).getBatchNumber());
    }

    @Test
    void findBatchByCategory_returnNotFoundException_whenBatchesNotExistsOnCategory() {
        // Arrange
        batches.clear();
        when(batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(ArgumentMatchers.anyInt(),
                ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(batches);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findBatchByCategory("FS"));

        // Assert
        assertThat(exception.getName()).contains("Products");
        assertEquals(exception.getMessage(), "There are no products in stock in the requested category");
    }

    @Test
    void findBatchByCategory_returnBadRequestException_whenInvalidCategory() {
        // Act
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.findBatchByCategory("ab"));

        // Assert
        assertThat(exception.getMessage()).contains("Invalid category, try again with one of the options");
    }
}