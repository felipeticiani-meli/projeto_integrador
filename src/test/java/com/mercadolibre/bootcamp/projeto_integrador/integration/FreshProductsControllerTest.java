package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.service.IInboundOrderService;
import com.mercadolibre.bootcamp.projeto_integrador.service.InboundOrderService;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.InboundOrderGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ProductsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.regex.Matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
class FreshProductsControllerTest extends BaseControllerTest {

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
    void findBatches_returnAll_whenCategoryNotInformed() throws Exception {
        service.create(validInboundOrderRequest, manager.getManagerId());
        mockMvc.perform(get("/api/v1/fresh-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(validInboundOrderRequest.getBatchStock().size()))
                .andExpect(jsonPath("$[0].batchNumber")
                        .value(validInboundOrderRequest.getBatchStock().get(0).getBatchNumber()))
                .andExpect(jsonPath("$[1].batchNumber")
                        .value(validInboundOrderRequest.getBatchStock().get(1).getBatchNumber()));
    }

    @Test
    void findBatches_returnNotFoundException_whenBatchNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name", containsString("Products")))
                .andExpect(jsonPath("$.message", containsString("There are no products in stock")));
    }

    @Test
    void findBatches_returnBatchesFresh_whenCategoryInformed() throws Exception {
        service.create(validInboundOrderRequest, manager.getManagerId());
        mockMvc.perform(get("/api/v1/fresh-products").param("category", "FS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(validInboundOrderRequest.getBatchStock().size()))
                .andExpect(jsonPath("$[0].batchNumber")
                        .value(validInboundOrderRequest.getBatchStock().get(0).getBatchNumber()))
                .andExpect(jsonPath("$[1].batchNumber")
                        .value(validInboundOrderRequest.getBatchStock().get(1).getBatchNumber()))
                .andExpect(jsonPath("$[0].category").value("FRESH"))
                .andExpect(jsonPath("$[1].category").value("FRESH"));
    }

    @Test
    void findBatches_returnNotFoundException_whenCategoryInformedAndBatchNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products").param("category", "FS"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name", containsString("Products")))
                .andExpect(jsonPath("$.message", containsString("There are no products in stock in the requested category")));
    }

    @Test
    void findBatches_returnBadRequestException_whenInvalidCategory() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products").param("category", "ab"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", containsString("Bad request")))
                .andExpect(jsonPath("$.message", containsString("Invalid category, try again with one of the options")));
    }
}