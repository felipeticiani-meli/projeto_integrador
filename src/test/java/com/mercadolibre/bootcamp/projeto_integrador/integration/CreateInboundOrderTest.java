package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class CreateInboundOrderTest extends BaseControllerTest {
    private Manager manager;
    private Manager forbiddenManager;
    private Section sectionWithChilled;
    private Product frozenProduct;

    private InboundOrderRequestDto validInboundOrderRequest;
    private InboundOrderRequestDto invalidInboundOrderRequest;

    @BeforeEach
    void setup() {
        Warehouse warehouse = getSavedWarehouse();

        manager = getSavedManager();
        forbiddenManager = getSavedManager();

        Section section = getSavedSection(warehouse, manager);
        sectionWithChilled = getSavedSection(warehouse, manager, Section.Category.CHILLED);

        Product product = getSavedProduct();
        frozenProduct = getSavedProduct(Section.Category.FROZEN);

        validInboundOrderRequest = getValidInboundOrderRequestDto(section, getValidBatchRequest(product));
        invalidInboundOrderRequest = getValidInboundOrderRequestDto(section, getInvalidBatchRequestDto(product));
    }

    @Test
    void createInboundOrder_returnsOk_whenIsGivenAValidInput() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenAnInvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(invalidInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenAManagerThatDoesNotHavePermission() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", forbiddenManager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInboundOrder_returnsError_whenIsNotGivenManagerIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Header Manager-Id is required"));
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenIncompatibleProducts() throws Exception {
        BatchRequestDto batchRequest = getValidBatchRequest(frozenProduct);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(sectionWithChilled, batchRequest);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Incompatible category"));
    }
}
