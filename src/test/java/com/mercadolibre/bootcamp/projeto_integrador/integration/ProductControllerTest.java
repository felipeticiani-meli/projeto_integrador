package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.service.IInboundOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
class ProductControllerTest extends BaseControllerTest {

    private Manager manager;
    private Warehouse warehouse;
    private Section section;
    private Product product;
    private InboundOrderRequestDto validInboundOrderRequest;

    @Autowired
    IInboundOrderService service;

    @BeforeEach
    public void setup() {
        warehouse = getSavedWarehouse();
        manager = getSavedManager();
        section = getSavedSection(warehouse, manager);
        product = getSavedProduct();
        validInboundOrderRequest = getValidInboundOrderRequestDtoWithBatchList(section, getValidListBatchRequest(product));
        validInboundOrderRequest.getBatchStock().get(0).setBatchNumber(1);
        validInboundOrderRequest.getBatchStock().get(1).setBatchNumber(2);
    }

    @Test
    void getProductDetails_returnProductWithBatches_whenValidProduct() throws Exception {
        service.create(validInboundOrderRequest, manager.getManagerId());
        long biggerBatchNumber = validInboundOrderRequest.getBatchStock().get(1).getBatchNumber();
        long smallerBatchNumber = validInboundOrderRequest.getBatchStock().get(0).getBatchNumber();
        mockMvc.perform(get("/api/v1/fresh-products/list")
                        .param("productId", String.valueOf(product.getProductId()))
                        .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getProductId()))
                .andExpect(jsonPath("$.batchStock").isNotEmpty())
                .andExpect(jsonPath("$.batchStock[*].section").isNotEmpty())
                .andExpect(jsonPath("$.batchStock[0].batchNumber").value(smallerBatchNumber))
                .andExpect(jsonPath("$.batchStock[1].batchNumber").value(biggerBatchNumber));
    }

    @Test
    void getProductDetails_returnNotFoundException_whenInvalidProduct() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/list")
                        .param("productId", String.valueOf(0))
                        .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name", containsString("not found")))
                .andExpect(jsonPath("$.message", containsString("There is no product with the specified id")));
    }

    @Test
    void getProductDetails_returnEmptyStockException_whenProductWithoutBatchStock() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/list")
                        .param("productId", String.valueOf(product.getProductId()))
                        .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("doesn't have stock")))
                .andExpect(jsonPath("$.message", containsString(product.getProductName())));
    }

    @Test
    void getProductDetails_returnOrderedByBatchNumber_whenValidProduct() throws Exception {
        service.create(validInboundOrderRequest, manager.getManagerId());
        long biggerBatchNumber = validInboundOrderRequest.getBatchStock().get(1).getBatchNumber();
        long smallerBatchNumber = validInboundOrderRequest.getBatchStock().get(0).getBatchNumber();
        mockMvc.perform(get("/api/v1/fresh-products/list")
                        .param("productId", String.valueOf(product.getProductId()))
                        .param("orderBy", "l")
                        .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getProductId()))
                .andExpect(jsonPath("$.batchStock").isNotEmpty())
                .andExpect(jsonPath("$.batchStock[*].section").isNotEmpty())
                .andExpect(jsonPath("$.batchStock[0].batchNumber").value(smallerBatchNumber))
                .andExpect(jsonPath("$.batchStock[1].batchNumber").value(biggerBatchNumber));
    }
}