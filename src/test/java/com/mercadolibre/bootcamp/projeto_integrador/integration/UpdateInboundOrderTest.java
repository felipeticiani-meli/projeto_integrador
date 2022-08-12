package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class UpdateInboundOrderTest extends BaseControllerTest {
    private Manager manager;
    private Manager forbiddenManager;
    private Section sectionWithChilled;
    private Product frozenProduct;
    private Product chilledProduct;
    private Product product;
    private Section section;
    private Section sectionWithMax1;

    private InboundOrderRequestDto validInboundOrderRequest;
    private BatchRequestDto validBatchRequest;

    @BeforeEach
    void setup() {
        Warehouse warehouse = getSavedWarehouse();

        manager = getSavedManager();
        forbiddenManager = getSavedManager();

        section = getSavedSection(warehouse, manager);
        sectionWithChilled = getSavedSection(warehouse, manager, Section.Category.CHILLED);
        sectionWithMax1 = getSavedSection(warehouse, manager, 1);

        product = getSavedProduct();
        frozenProduct = getSavedProduct(Section.Category.FROZEN);
        chilledProduct = getSavedProduct(Section.Category.CHILLED);

        validBatchRequest = getValidBatchRequest(product);
        validInboundOrderRequest = getValidInboundOrderRequestDto(section, validBatchRequest);
    }

    @Test
    void updateInboundOrder_returnsOk_whenBatchExists() throws Exception {
        InboundOrder order = getSavedInboundOrder(section);
        Batch batch = getSavedBatch(product, order);

        BatchRequestDto batchRequest = getValidBatchRequest(product);
        float newTemperature = batchRequest.getCurrentTemperature() + 1;
        batchRequest.setBatchNumber(batch.getBatchNumber());
        batchRequest.setCurrentTemperature(newTemperature);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + order.getOrderNumber())
                        .content(asJsonString(getValidInboundOrderRequestDto(section, batchRequest)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        batch = batchRepository.findById(batch.getBatchNumber()).get();
        assertThat(batch.getCurrentTemperature()).isEqualTo(newTemperature);
    }

    @Test
    void updateInboundOrder_returnsOk_whenBatchNotExists() throws Exception {
        InboundOrder order = getSavedInboundOrder(section);

        MvcResult response = mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + order.getOrderNumber())
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        InboundOrderResponseDto responseDto = objectMapper.readValue(json, InboundOrderResponseDto.class);

        Batch batch = batchRepository.findById(responseDto.getBatchStock().get(0).getBatchNumber()).get();
        assertThat(batch).isNotNull();
        assertThat(batch.getInboundOrder().getOrderNumber()).isEqualTo(order.getOrderNumber());
    }

    @Test
    void updateInboundOrder_returnsNotFound_whenInboundOrderIdNotExists() throws Exception {
        InboundOrder order = getSavedInboundOrder(section);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + order.getOrderNumber() + 1)
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("Inbound Order not found."));
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenSectionDoesNotHaveEnoughSpace() throws Exception {
        InboundOrder order = getSavedInboundOrder(sectionWithMax1);
        getSavedBatch(product, order);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + order.getOrderNumber())
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Section"))
                .andExpect(jsonPath("$.message", containsString("enough space")));
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenInvalidInitialQuantity() throws Exception {
        InboundOrder order = getSavedInboundOrder(section);
        Batch batch = BatchGenerator.newBatch(product, order);
        batch.setCurrentQuantity(batch.getInitialQuantity() - 5);
        batchRepository.save(batch);

        BatchRequestDto batchRequest = getValidBatchRequest(product);

        batchRequest.setBatchNumber(1L);
        batchRequest.setInitialQuantity(batch.getInitialQuantity() - (batch.getCurrentQuantity() + 1));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + order.getOrderNumber())
                        .content(asJsonString(getValidInboundOrderRequestDto(section, batchRequest)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Invalid batch quantity"))
                .andExpect(jsonPath("$.message", containsString("update batch initial quantity")));
    }

    @Test
    void updateInboundOrder_returnsForbidden_whenIsGivenAManagerThatDoesNotHavePermission() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(validInboundOrderRequest))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        InboundOrder order = inboundOrderRepository.findAll().get(0);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(order.getOrderNumber()))
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", forbiddenManager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsNotGivenManagerIdHeader() throws Exception {
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
    void updateInboundOrder_returnsBadRequest_whenIsGivenIncompatibleProducts() throws Exception {
        BatchRequestDto batchRequest = getValidBatchRequest(chilledProduct);
        BatchRequestDto incompatibleBatchRequest = getValidBatchRequest(frozenProduct);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(sectionWithChilled, batchRequest);
        InboundOrderRequestDto incompatibleRequestDto = getValidInboundOrderRequestDto(sectionWithChilled,
                incompatibleBatchRequest);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(requestDto))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(incompatibleRequestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Incompatible category"));
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsGivenBatchesOfDifferentOrders() throws Exception {
        InboundOrder order1 = getSavedInboundOrder(section);
        InboundOrder order2 = getSavedInboundOrder(section);

        Batch batch1 = getSavedBatch(product, order1);
        Batch batch2 = getSavedBatch(product, order2);

        BatchRequestDto request1 = modelMapper.map(batch1, BatchRequestDto.class);
        BatchRequestDto request2 = modelMapper.map(batch2, BatchRequestDto.class);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(getValidInboundOrderRequestDto(section, request1, request2)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsGivenProductThatDoesNotExists() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(validInboundOrderRequest))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        BatchRequestDto batchWithNonExistentProduct = getBatchRequest(999);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(getValidInboundOrderRequestDto(section, batchWithNonExistentProduct)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
