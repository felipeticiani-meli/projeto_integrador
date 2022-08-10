package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class UpdateInboundOrderTest extends BaseControllerTest {
    @Test
    void updateInboundOrder_returnCreated_whenBatchExists() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager);
        Product product = getSavedProduct();

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getValidBatchRequest(product);

        // From BatchRequestDto map Batch to save in DB
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(section);
        ib = inboundOrderRepository.save(ib);

        // Save Batch in DB
        batch.setInboundOrder(ib);
        batch = batchRepository.save(batch);

        // Get the current temperature from batch to make a PUT to update it (specify BatchNumber).
        float newTemperature = batchRequest.getCurrentTemperature() + 1;
        batchRequest.setBatchNumber(batch.getBatchNumber());
        batchRequest.setCurrentTemperature(newTemperature);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + ib.getOrderNumber())
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        batch = batchRepository.findById(batch.getBatchNumber()).get();
        assertThat(batch.getCurrentTemperature()).isEqualTo(newTemperature);
    }

    @Test
    void updateInboundOrder_returnCreated_whenBatchNotExists() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager);
        Product product = getSavedProduct();

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getValidBatchRequest(product);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(section);
        ib = inboundOrderRepository.save(ib);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        MvcResult response = mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + ib.getOrderNumber())
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        InboundOrderResponseDto responseDto = objectMapper.readValue(json, InboundOrderResponseDto.class);

        Batch batch = batchRepository.findById(responseDto.getBatchStock().get(0).getBatchNumber()).get();
        assertThat(batch).isNotNull();
        assertThat(batch.getInboundOrder().getOrderNumber()).isEqualTo(ib.getOrderNumber());
    }

    @Test
    void updateInboundOrder_returnCreated_whenInboundOrderIdNotExists() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager);
        Product product = getSavedProduct();

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getValidBatchRequest(product);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(section);
        ib = inboundOrderRepository.save(ib);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + ib.getOrderNumber() + 1)
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("Inbound Order not found."));
    }

    @Test
    void updateInboundOrder_returnCreated_whenSectionDoesNotHaveEnoughSpace() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager, 1);
        Product product = getSavedProduct();

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getValidBatchRequest(product);

        // From BatchRequestDto map Batch to save in DB
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(section);
        ib = inboundOrderRepository.save(ib);

        // Save Batch in DB
        batch.setInboundOrder(ib);
        batchRepository.save(batch);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + ib.getOrderNumber())
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Section"))
                .andExpect(jsonPath("$.message", containsString("enough space")));
    }

    @Test
    void updateInboundOrder_returnCreated_whenInvalidInitialQuantity() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager);
        Product product = getSavedProduct();

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getValidBatchRequest(product);

        // From BatchRequestDto map Batch to save in DB
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);
        batch.setCurrentQuantity(batch.getInitialQuantity() - 5);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(section);
        ib = inboundOrderRepository.save(ib);

        // Save Batch in DB
        batch.setInboundOrder(ib);
        batchRepository.save(batch);

        // Change initial quantity to less than what have been sold
        batchRequest.setBatchNumber(1L);
        batchRequest.setInitialQuantity(batch.getInitialQuantity() - (batch.getCurrentQuantity() + 1));

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + ib.getOrderNumber())
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Invalid batch quantity"))
                .andExpect(jsonPath("$.message", containsString("update batch initial quantity")));
    }

    @Test
    void updateInboundOrder_returnsError_whenIsGivenAManagerThatDoesNotHavePermission() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Manager forbiddenManager = getSavedManager();
        Section section = getSavedSection(warehouse, manager);
        Product product = getSavedProduct();

        BatchRequestDto batchRequest = getValidBatchRequest(product);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(section, batchRequest);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        InboundOrder order = inboundOrderRepository.findAll().get(0);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(order.getOrderNumber()))
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", forbiddenManager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateInboundOrder_returnsError_whenIsNotGivenManagerIdHeader() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager);
        Product product = getSavedProduct();

        BatchRequestDto batchRequest = getValidBatchRequest(product);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(section, batchRequest);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Header Manager-Id is required"));
    }

    @Test
    void updateInboundOrder_returnsError_whenIsGivenIncompatibleProducts() throws Exception {
        Warehouse warehouse = getSavedWarehouse();
        Manager manager = getSavedManager();
        Section section = getSavedSection(warehouse, manager, Section.Category.CHILLED);
        Product product = getSavedProduct(Section.Category.CHILLED);
        Product incompatibleProduct = getSavedProduct(Section.Category.FROZEN);

        BatchRequestDto batchRequest = getValidBatchRequest(product);
        BatchRequestDto incompatibleBatchRequest = getValidBatchRequest(incompatibleProduct);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(section, batchRequest);
        InboundOrderRequestDto incompatibleRequestDto = getValidInboundOrderRequestDto(section, incompatibleBatchRequest);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(incompatibleRequestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Incompatible category"));
    }
}
