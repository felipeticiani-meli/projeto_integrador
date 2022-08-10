package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import com.mercadolibre.bootcamp.projeto_integrador.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class BaseControllerTest {
    protected final ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ISectionRepository sectionRepository;
    @Autowired
    protected IWarehouseRepository warehouseRepository;
    @Autowired
    protected IManagerRepository managerRepository;
    @Autowired
    protected IProductRepository productRepository;
    @Autowired
    protected IBatchRepository batchRepository;
    @Autowired
    protected IInboundOrderRepository inboundOrderRepository;

    public BaseControllerTest() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    protected InboundOrderRequestDto getValidInboundOrderRequestDto(Section section, BatchRequestDto batchRequest) {
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setBatchStock(List.of(batchRequest));
        requestDto.setSectionCode(section.getSectionCode());
        return requestDto;
    }

    protected BatchRequestDto getInvalidBatchRequestDto(Product product) {
        BatchRequestDto batchRequest = new BatchRequestDto();
        batchRequest.setProductId(product.getProductId());

        // Valores inválidos.
        batchRequest.setProductPrice(new BigDecimal("-100.99"));
        batchRequest.setCurrentTemperature(-1);
        batchRequest.setMinimumTemperature(-1);
        batchRequest.setDueDate(LocalDate.now().minusWeeks(1));
        batchRequest.setManufacturingTime(LocalDateTime.now().plusDays(1));
        batchRequest.setManufacturingDate(LocalDate.now().plusDays(1));
        batchRequest.setInitialQuantity(-1);
        return batchRequest;
    }

    protected BatchRequestDto getValidBatchRequest(Product product) {
        BatchRequestDto batchRequest = BatchGenerator.newBatchRequestDTO();
        batchRequest.setProductId(product.getProductId());
        return batchRequest;
    }

    protected Warehouse getSavedWarehouse() {
        Warehouse warehouse = WarehouseGenerator.newWarehouse();
        warehouseRepository.save(warehouse);
        return warehouse;
    }

    protected Product getSavedProduct() {
        return getSavedProduct(Section.Category.FRESH);
    }

    protected Product getSavedProduct(Section.Category category) {
        Product product = ProductsGenerator.newProductFresh();
        product.setCategory(category);
        productRepository.save(product);
        return product;
    }

    protected Section getSavedSection(Warehouse warehouse, Manager manager) {
        return getSavedSection(warehouse, manager, 10);
    }

    protected Section getSavedSection(Warehouse warehouse, Manager manager, int maxBatches) {
        Section section = SectionGenerator.getSection(warehouse, manager);
        section.setMaxBatches(maxBatches);
        sectionRepository.save(section);
        return section;
    }

    protected Section getSavedSection(Warehouse warehouse, Manager manager, Section.Category category) {
        Section section = SectionGenerator.getSection(warehouse, manager);
        section.setCategory(category);
        sectionRepository.save(section);
        return section;
    }

    protected Manager getSavedManager() {
        Manager manager = ManagerGenerator.newManager();
        managerRepository.save(manager);
        return manager;
    }

    protected String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
