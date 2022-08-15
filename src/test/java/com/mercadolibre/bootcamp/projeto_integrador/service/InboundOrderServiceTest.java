package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import com.mercadolibre.bootcamp.projeto_integrador.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundOrderServiceTest {
    @InjectMocks
    private InboundOrderService inboundService;

    @Mock
    IProductRepository productRepository;

    @Mock
    ISectionRepository sectionRepository;

    @Mock
    IInboundOrderRepository inboundOrderRepository;

    @Mock
    IBatchRepository batchRepository;

    @Mock
    IManagerRepository managerRepository;

    @Mock
    IBatchService batchService;

    private final long managerId = ManagerGenerator.getManagerWithId().getManagerId();
    private final long orderNumber = 1l;
    private InboundOrderRequestDto inboundOrderRequest;

    @BeforeEach
    private void setup() {
        inboundOrderRequest = InboundOrderGenerator.newInboundRequestDTO();
    }

    @Test
    void create_returnsException_whenProductCategoryNotCompatibleWithSection() {
        // Arrange
        inboundOrderRequest.getBatchStock().get(0).setProductId(2);
        when(sectionRepository.findById(inboundOrderRequest.getSectionCode()))
                .thenReturn(Optional.of(SectionGenerator.getFreshSectionWith1SlotAvailable()));
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(List.of(ProductsGenerator.newProductChilled()));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inboundService.create(inboundOrderRequest, managerId)
        );

        // Assert
        assertThat(exception.getMessage()).contains("The following products have incompatible category with the section:");
        verify(inboundOrderRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void create_returnsBatches_whenSectionHasExactSpaceAvailable() {
        // Arrange
        Product productFresh = ProductsGenerator.newProductFresh();
        productFresh.setProductId(1);
        when(sectionRepository.findById(inboundOrderRequest.getSectionCode()))
                .thenReturn(Optional.of(SectionGenerator.getFreshSectionWith1SlotAvailable()));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(List.of(productFresh));
        when(sectionRepository.save(ArgumentMatchers.any(Section.class)))
                .thenReturn(null);
        when(inboundOrderRepository.save(ArgumentMatchers.any(InboundOrder.class)))
                .thenReturn(null);
        when(batchRepository.saveAll(ArgumentMatchers.anyList()))
                .thenReturn(null);

        // Act
        InboundOrderResponseDto inboundResponse = inboundService.create(inboundOrderRequest, managerId);

        // Assert
        assertThat(inboundResponse).isNotNull();
        Batch batchResponse = inboundResponse.getBatchStock().get(0);
        BatchRequestDto batchRequest = inboundOrderRequest.getBatchStock().get(0);
        assertThat(batchResponse.getProduct().getProductId()).isEqualTo(batchRequest.getProductId());
        assertThat(batchResponse.getCurrentTemperature()).isEqualTo(batchRequest.getCurrentTemperature());
        assertThat(batchResponse.getMinimumTemperature()).isEqualTo(batchRequest.getMinimumTemperature());
        assertThat(batchResponse.getInitialQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getCurrentQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getManufacturingDate()).isEqualTo(batchRequest.getManufacturingDate());
        assertThat(batchResponse.getManufacturingTime()).isEqualTo(batchRequest.getManufacturingTime());
        assertThat(batchResponse.getDueDate()).isEqualTo(batchRequest.getDueDate());
        assertThat(batchResponse.getProductPrice()).isEqualTo(batchRequest.getProductPrice());
    }

    @Test
    void create_returnsBatches_whenSectionHasMoreSpaceAvailable() {
        // Arrange
        inboundOrderRequest.setBatchStock(BatchGenerator.newList2BatchRequestsDTO());
        List<Product> products = new ArrayList<>();
        products.add(ProductsGenerator.newProductFresh());
        products.add(ProductsGenerator.newProductFresh());
        products.get(0).setProductId(2);
        products.get(1).setProductId(3);
        when(sectionRepository.findById(inboundOrderRequest.getSectionCode()))
                .thenReturn(Optional.of(SectionGenerator.getFreshSectionWith10SlotsAvailable()));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(products);
        when(sectionRepository.save(ArgumentMatchers.any(Section.class)))
                .thenReturn(null);
        when(inboundOrderRepository.save(ArgumentMatchers.any(InboundOrder.class)))
                .thenReturn(null);
        when(batchRepository.saveAll(ArgumentMatchers.anyList()))
                .thenReturn(null);

        // Act
        InboundOrderResponseDto inboundResponse = inboundService.create(inboundOrderRequest, managerId);

        // Assert
        assertThat(inboundResponse).isNotNull();
        Batch batchResponse = inboundResponse.getBatchStock().get(0);
        BatchRequestDto batchRequest = inboundOrderRequest.getBatchStock().get(0);
        assertThat(batchResponse.getProduct().getProductId()).isEqualTo(batchRequest.getProductId());
        assertThat(batchResponse.getCurrentTemperature()).isEqualTo(batchRequest.getCurrentTemperature());
        assertThat(batchResponse.getMinimumTemperature()).isEqualTo(batchRequest.getMinimumTemperature());
        assertThat(batchResponse.getInitialQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getCurrentQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getManufacturingDate()).isEqualTo(batchRequest.getManufacturingDate());
        assertThat(batchResponse.getManufacturingTime()).isEqualTo(batchRequest.getManufacturingTime());
        assertThat(batchResponse.getDueDate()).isEqualTo(batchRequest.getDueDate());
        assertThat(batchResponse.getProductPrice()).isEqualTo(batchRequest.getProductPrice());
        assertThat(inboundResponse.getBatchStock().get(1).getCurrentQuantity())
                .isEqualTo(inboundOrderRequest.getBatchStock().get(1).getInitialQuantity());
    }


    @Test
    void update_returnsException_whenInboundOrderNoExist() {
        // Arrange
        when(inboundOrderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inboundService.update(orderNumber, inboundOrderRequest, managerId)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("There is no inbound order with the specified id");
        verify(batchService, never())
                .updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList());
    }

    @Test
    void update_returnsException_whenManagerInvalid() {
        // Arrange
        long managerIdInvalid = 99l;
        when(inboundOrderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(InboundOrderGenerator.newFreshInboundOrder()));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inboundService.update(orderNumber, inboundOrderRequest, managerIdInvalid)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("Manager with id " + managerIdInvalid + " not found");
        verify(batchService, never())
                .updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList());
    }

    @Test
    void update_returnsException_whenManagerNotHaveOrderPermission() {
        // Arrange
        long firstManagerId = 1l;
        InboundOrder savedInboundOrderByFirstManager = InboundOrderGenerator.newFreshInboundOrder();
        savedInboundOrderByFirstManager.getSection().getManager().setManagerId(firstManagerId);

        long unauthorizedManagerId = 2l;
        Manager unauthorizedManager = ManagerGenerator.newManager();
        unauthorizedManager.setManagerId(unauthorizedManagerId);

        when(inboundOrderRepository.findById(savedInboundOrderByFirstManager.getOrderNumber()))
                .thenReturn(Optional.of(savedInboundOrderByFirstManager));
        when(managerRepository.findById(unauthorizedManagerId))
                .thenReturn(Optional.of(unauthorizedManager));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inboundService.update(orderNumber, inboundOrderRequest, unauthorizedManagerId)
        );

        // Assert
        assertThat(exception.getMessage()).contains("is not authorized to perform this action.");
        assertThat(exception.getMessage()).contains(unauthorizedManager.getName());
        verify(batchService, never())
                .updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList());
    }

    @Test
    void update_returnsException_whenSectionHasNoSpaceToAdd() {
        // Arrange
        InboundOrder inboundOrder = InboundOrderGenerator.newFreshInboundOrder();
        inboundOrder.getSection().setCurrentBatches(inboundOrder.getSection().getMaxBatches());
        when(inboundOrderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(inboundOrder));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inboundService.update(orderNumber, inboundOrderRequest, managerId)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("Section does not have enough space");
        verify(batchService, never())
                .updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList());
    }

    @Test
    void update_returnsException_whenProductCategoryNotCompatibleWithSection() {
        // Arrange
        inboundOrderRequest.getBatchStock().get(0).setProductId(2);
        when(inboundOrderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(InboundOrderGenerator.newFreshInboundOrder()));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(List.of(ProductsGenerator.newProductChilled()));

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> inboundService.update(orderNumber, inboundOrderRequest, managerId)
        );

        // Assert
        assertThat(exception.getMessage()).contains("The following products have incompatible category with the section:");
        verify(batchService, never())
                .updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList());
    }

    @Test
    void update_returnsBatches_whenSectionHasExactSpaceAvailableToAddBatches() {
        // Arrange
        Product productFreshWithId = ProductsGenerator.newProductFresh();
        productFreshWithId.setProductId(1l);
        InboundOrder savedInboundOrder = InboundOrderGenerator.newFreshInboundOrder();
        savedInboundOrder.setSection(SectionGenerator.getFreshSectionWith1SlotAvailable());
        List<Batch> batches = List.of(BatchGenerator.mapBatchRequestDtoToBatch(inboundOrderRequest.getBatchStock().get(0)));
        batches.get(0).setCurrentQuantity(batches.get(0).getInitialQuantity());
        when(inboundOrderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(savedInboundOrder));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(List.of(productFreshWithId));
        when(batchService.updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList()))
                .thenReturn(batches);
        when(sectionRepository.save(ArgumentMatchers.any(Section.class)))
                .thenReturn(null);

        // Act
        InboundOrderResponseDto inboundResponse = inboundService.update(orderNumber, inboundOrderRequest, managerId);

        // Assert
        assertThat(inboundResponse).isNotNull();
        Batch batchResponse = inboundResponse.getBatchStock().get(0);
        BatchRequestDto batchRequest = inboundOrderRequest.getBatchStock().get(0);
        assertThat(batchResponse.getBatchNumber()).isEqualTo(batchRequest.getBatchNumber());
        assertThat(batchResponse.getCurrentTemperature()).isEqualTo(batchRequest.getCurrentTemperature());
        assertThat(batchResponse.getMinimumTemperature()).isEqualTo(batchRequest.getMinimumTemperature());
        assertThat(batchResponse.getInitialQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getCurrentQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getManufacturingDate()).isEqualTo(batchRequest.getManufacturingDate());
        assertThat(batchResponse.getManufacturingTime()).isEqualTo(batchRequest.getManufacturingTime());
        assertThat(batchResponse.getDueDate()).isEqualTo(batchRequest.getDueDate());
        assertThat(batchResponse.getProductPrice()).isEqualTo(batchRequest.getProductPrice());
    }

    @Test
    void update_returnsBatches_whenUpdatingBatchInSectionThatHasNoMoreSpace() {
        // Arrange
        inboundOrderRequest.getBatchStock().get(0).setBatchNumber(1l);
        Product productFresh = ProductsGenerator.newProductFresh();
        productFresh.setProductId(1l);
        InboundOrder savedInboundOrder = InboundOrderGenerator.newFreshInboundOrder();
        savedInboundOrder.getSection().setCurrentBatches(
                savedInboundOrder.getSection().getMaxBatches()
        );
        List<Batch> batches = List.of(BatchGenerator.mapBatchRequestDtoToBatch(inboundOrderRequest.getBatchStock().get(0)));
        batches.get(0).setCurrentQuantity(batches.get(0).getInitialQuantity());
        when(inboundOrderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(savedInboundOrder));
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ManagerGenerator.getManagerWithId()));
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(List.of(productFresh));
        when(batchService.updateAll(ArgumentMatchers.any(InboundOrder.class), ArgumentMatchers.anyList()))
                .thenReturn(batches);
        when(sectionRepository.save(ArgumentMatchers.any(Section.class)))
                .thenReturn(null);

        // Act
        InboundOrderResponseDto inboundResponse = inboundService.update(orderNumber, inboundOrderRequest, managerId);

        // Assert
        assertThat(inboundResponse).isNotNull();
        Batch batchResponse = inboundResponse.getBatchStock().get(0);
        BatchRequestDto batchRequest = inboundOrderRequest.getBatchStock().get(0);
        assertThat(batchResponse.getBatchNumber()).isEqualTo(batchRequest.getBatchNumber());
        assertThat(batchResponse.getCurrentTemperature()).isEqualTo(batchRequest.getCurrentTemperature());
        assertThat(batchResponse.getMinimumTemperature()).isEqualTo(batchRequest.getMinimumTemperature());
        assertThat(batchResponse.getInitialQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getCurrentQuantity()).isEqualTo(batchRequest.getInitialQuantity());
        assertThat(batchResponse.getManufacturingDate()).isEqualTo(batchRequest.getManufacturingDate());
        assertThat(batchResponse.getManufacturingTime()).isEqualTo(batchRequest.getManufacturingTime());
        assertThat(batchResponse.getDueDate()).isEqualTo(batchRequest.getDueDate());
        assertThat(batchResponse.getProductPrice()).isEqualTo(batchRequest.getProductPrice());
    }
}