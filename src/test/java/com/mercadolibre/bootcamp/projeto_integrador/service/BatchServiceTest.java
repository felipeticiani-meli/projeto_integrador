package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchDueDateResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.ManagerNotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.UnauthorizedManagerException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ManagerGenerator;
import org.aspectj.weaver.ast.Not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @InjectMocks
    private BatchService service;

    @Mock
    private IBatchRepository batchRepository;
    @Mock
    private ISectionRepository sectionRepository;
    @Mock
    private IManagerRepository managerRepository;

    private List<Batch> batches;
    private Section section;
    private Manager manager;

    @BeforeEach
    private void setup() {
        batches = BatchGenerator.newBatchList();
        section = batches.get(0).getInboundOrder().getSection();
        manager = section.getManager();
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

    @Test
    void findBatchBySection_returnBatches_whenBatchesExists() {
        // Arrange
        batches.get(0).setCurrentQuantity(0);
        batches.get(1).setDueDate(LocalDate.now().plusDays(5));
        when(sectionRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(section));
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(manager));
        when((batchRepository.findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any()))).thenReturn(batches);

        // Act
        List<BatchDueDateResponseDto> returnedBatches = service.findBatchBySection(section.getSectionCode(), 15, manager.getManagerId());

        // Assert
        assertThat(returnedBatches).isNotEmpty();
        assertEquals(returnedBatches.size(), 2);
        assertEquals(returnedBatches.get(0).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(returnedBatches.get(1).getBatchNumber(), batches.get(2).getBatchNumber());
        assertEquals(returnedBatches.get(0).getProductName(), batches.get(1).getProduct().getProductName());
        assertEquals(returnedBatches.get(1).getProductName(), batches.get(2).getProduct().getProductName());
        assertThat(returnedBatches.get(0).getCurrentQuantity()).isPositive();
        assertThat(returnedBatches.get(1).getCurrentQuantity()).isPositive();
        assertThat(returnedBatches.get(0).getDueDate()).isBefore(LocalDate.now().plusDays(16));
        assertThat(returnedBatches.get(1).getDueDate()).isBefore(LocalDate.now().plusDays(16));
        assertThat(returnedBatches.get(0).getDueDate()).isBefore(returnedBatches.get(1).getDueDate());
    }

    @Test
    void findBatchBySection_returnNotFoundException_whenInvalidSection() {
        // Arrange
        when(sectionRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.findBatchBySection(section.getSectionCode(), 15, manager.getManagerId()));

        // Assert
        assertThat(exception.getName()).contains("section");
        assertThat(exception.getMessage()).contains("There is no section with the specified id");
        verify(batchRepository, never()).findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void findBatchBySection_returnEmptyList_whenBatchesNotExistsForParameters() {
        // Arrange
        batches.clear();
        when(sectionRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(section));
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(manager));
        when((batchRepository.findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any()))).thenReturn(batches);

        // Act
        List<BatchDueDateResponseDto> returnedBatches = service.findBatchBySection(section.getSectionCode(), 15, manager.getManagerId());

        // Assert
        assertThat(returnedBatches).isEmpty();
    }

    @Test
    void findBatchBySection_returnNotFoundException_whenInvalidManager() {
        // Arrange
        when(sectionRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(section));
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        // Act
        ManagerNotFoundException exception = assertThrows(ManagerNotFoundException.class,
                () -> service.findBatchBySection(section.getSectionCode(), 15, manager.getManagerId()));

        // Assert
        assertThat(exception.getName()).contains("Manager not found");
        assertThat(exception.getMessage()).contains("Manager with id " + manager.getManagerId() + " not found");
        verify(batchRepository, never()).findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void findBatchBySection_returnUnauthorizedManagerException_whenUnauthorizedManager() {
        // Arrange
        Manager unauthorizedManager = ManagerGenerator.newManager();
        unauthorizedManager.setManagerId(5);
        when(sectionRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(section));
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(unauthorizedManager));

        // Act
        UnauthorizedManagerException exception = assertThrows(UnauthorizedManagerException.class,
                () -> service.findBatchBySection(section.getSectionCode(), 15, unauthorizedManager.getManagerId()));

        // Assert
        assertThat(exception.getName()).contains(unauthorizedManager.getName() + " is not authorized.");
        assertThat(exception.getMessage()).contains(unauthorizedManager.getName() + " is not authorized to perform this action");
        verify(batchRepository, never()).findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void findBatchBySection_returnBadRequestException_whenInvalidNumberOfDays() {
        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.findBatchBySection(section.getSectionCode(), -1, manager.getManagerId()));

        // Assert
        assertThat(exception.getName()).contains("Bad request");
        assertThat(exception.getMessage()).contains("The number of days to expiration can't be negative");
        verify(batchRepository, never()).findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void findBatchByCategoryAndDueDate_returnFreshBatchesAscOrder_whenBatchesExists() {
        // Arrange
        batches.get(0).setCurrentQuantity(0);
        batches.get(1).setDueDate(LocalDate.now().plusDays(5));
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(manager));
        when(batchRepository.findByProduct_CategoryAndDueDateBetweenOrderByDueDateAsc(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(batches);

        // Act
        List<BatchDueDateResponseDto> returnedBatches = service.findBatchByCategoryAndDueDate("FS", 15,
                "ASC", manager.getManagerId());

        // Assert
        assertThat(returnedBatches).isNotEmpty();
        assertEquals(returnedBatches.size(), 2);
        assertEquals(returnedBatches.get(0).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(returnedBatches.get(1).getBatchNumber(), batches.get(2).getBatchNumber());
        assertEquals(returnedBatches.get(0).getProductName(), batches.get(1).getProduct().getProductName());
        assertEquals(returnedBatches.get(1).getProductName(), batches.get(2).getProduct().getProductName());
        assertThat(returnedBatches.get(0).getCurrentQuantity()).isPositive();
        assertThat(returnedBatches.get(1).getCurrentQuantity()).isPositive();
        assertThat(returnedBatches.get(0).getDueDate()).isBefore(LocalDate.now().plusDays(16));
        assertThat(returnedBatches.get(1).getDueDate()).isBefore(LocalDate.now().plusDays(16));
        assertThat(returnedBatches.get(0).getDueDate()).isBefore(returnedBatches.get(1).getDueDate());
    }

    @Test
    void findBatchByCategoryAndDueDate_returnChilledBatchesDescOrder_whenBatchesExists() {
        // Arrange
        batches = BatchGenerator.newBatchListChilled();
        batches.get(0).setCurrentQuantity(0);
        batches.get(1).setDueDate(LocalDate.now().plusDays(8));
        manager.setManagerId(1);
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(manager));
        when(batchRepository.findByProduct_CategoryAndDueDateBetweenOrderByDueDateDesc(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(batches);

        // Act
        List<BatchDueDateResponseDto> returnedBatches = service.findBatchByCategoryAndDueDate("RF", 15,
                "DESC", manager.getManagerId());

        // Assert
        assertThat(returnedBatches).isNotEmpty();
        assertEquals(returnedBatches.size(), 2);
        assertEquals(returnedBatches.get(0).getBatchNumber(), batches.get(1).getBatchNumber());
        assertEquals(returnedBatches.get(1).getBatchNumber(), batches.get(2).getBatchNumber());
        assertEquals(returnedBatches.get(0).getProductName(), batches.get(1).getProduct().getProductName());
        assertEquals(returnedBatches.get(1).getProductName(), batches.get(2).getProduct().getProductName());
        assertThat(returnedBatches.get(0).getCurrentQuantity()).isPositive();
        assertThat(returnedBatches.get(1).getCurrentQuantity()).isPositive();
        assertThat(returnedBatches.get(0).getDueDate()).isBefore(LocalDate.now().plusDays(16));
        assertThat(returnedBatches.get(1).getDueDate()).isBefore(LocalDate.now().plusDays(16));
        assertThat(returnedBatches.get(0).getDueDate()).isAfter(returnedBatches.get(1).getDueDate());
    }

    @Test
    void findBatchByCategoryAndDueDate_returnEmptyList_whenBatchesNotExistsForParameters() {
        // Arrange
        batches.clear();
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(manager));
        when(batchRepository.findByProduct_CategoryAndDueDateBetweenOrderByDueDateAsc(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(batches);

        // Act
        List<BatchDueDateResponseDto> returnedBatches = service.findBatchByCategoryAndDueDate("FS", 15,
                "ASC", manager.getManagerId());

        // Assert
        assertThat(returnedBatches).isEmpty();
    }

    @Test
    void findBatchByCategoryAndDueDate_returnNotFoundException_whenInvalidManager() {
        // Arrange
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        // Act
        ManagerNotFoundException exception = assertThrows(ManagerNotFoundException.class,
                () -> service.findBatchByCategoryAndDueDate("FS", 15, "ASC", manager.getManagerId()));

        // Assert
        assertThat(exception.getName()).contains("Manager not found");
        assertThat(exception.getMessage()).contains("Manager with id " + manager.getManagerId() + " not found");
        verify(batchRepository, never()).findByProduct_CategoryAndDueDateBetweenOrderByDueDateAsc(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void findBatchByCategoryAndDueDate_returnBadRequestException_whenInvalidNumberOfDays() {
        // Arrange
        when(managerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(manager));

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> service.findBatchByCategoryAndDueDate("FS", -1, "ASC", manager.getManagerId()));

        // Assert
        assertThat(exception.getName()).contains("Bad request");
        assertThat(exception.getMessage()).contains("The number of days to expiration can't be negative");
        verify(batchRepository, never()).findByProduct_CategoryAndDueDateBetweenOrderByDueDateAsc(ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }
}