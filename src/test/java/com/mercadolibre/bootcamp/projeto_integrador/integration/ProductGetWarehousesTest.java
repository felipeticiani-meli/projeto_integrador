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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
class ProductGetWarehousesTest extends BaseControllerTest {

    private Manager manager;
    private Product product1, product2;
    private InboundOrderRequestDto validInboundOrderRequestSection1, validInboundOrderRequestSection2, validInboundOrderRequestSection3;

    @Autowired
    IInboundOrderService service;

    @BeforeEach
    public void setup() {
        manager = getSavedManager();
        product1 = getSavedProduct(Section.Category.FRESH);
        product2 = getSavedProduct(Section.Category.FRESH);

        Warehouse warehouse = getSavedWarehouseWithoutCode();
        Section section = getSavedSection(warehouse, manager, product1.getCategory());
        validInboundOrderRequestSection1 = getValidInboundOrderRequestDtoWithBatchList(section, getValidListBatchRequest(product1));

        warehouse = getSavedWarehouseWithoutCode();
        section = getSavedSection(warehouse, manager, product1.getCategory());
        validInboundOrderRequestSection2 = getValidInboundOrderRequestDtoWithBatchList(section, getValidListBatchRequest(product1));

        warehouse = getSavedWarehouseWithoutCode();
        section = getSavedSection(warehouse, manager, product1.getCategory());
        validInboundOrderRequestSection3 = getValidInboundOrderRequestDtoWithBatchList(section, getValidListBatchRequest(product1));

        service.create(validInboundOrderRequestSection1, manager.getManagerId());
        service.create(validInboundOrderRequestSection2, manager.getManagerId());
        service.create(validInboundOrderRequestSection3, manager.getManagerId());
    }

    @Test
    void getWarehouses_returnAll_whenValidProductId() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/warehouse")
                .param("productId", String.valueOf(product1.getProductId()))
                .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product1.getProductId()))
                .andExpect(jsonPath("$.warehouses.length()").value(3))
                .andExpect(jsonPath("$.warehouses[0].totalQuantity").value(validInboundOrderRequestSection1.getBatchStock()
                        .stream().mapToInt(i -> i.getInitialQuantity()).sum()));
    }

    @Test
    void getWarehouses_returnNotFoundException_whenProductNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/warehouse")
                .param("productId", String.valueOf(product1.getProductId()+1000))
                .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name", containsString("Product")))
                .andExpect(jsonPath("$.message", containsString("There is no product")));
    }

    @Test
    void getWarehouses_returnNotFoundException_whenManagerNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/warehouse")
                .param("productId", String.valueOf(product1.getProductId()))
                .header("Manager-Id", manager.getManagerId()+1000))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name", containsString("Manager")))
                .andExpect(jsonPath("$.message", containsString("Manager with id")));
    }

    @Test
    void getWarehouses_returnAll_whenProductIsNotInAnyWarehouse() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/warehouse")
                .param("productId", String.valueOf(product2.getProductId()))
                .header("Manager-Id", manager.getManagerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product2.getProductId()))
                .andExpect(jsonPath("$.warehouses.length()").value(0));
    }
}