package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class BatchControllerTest extends BaseControllerTest {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Manager manager;
    private Manager forbiddenManager;
    private Section section;
    private InboundOrder order;
    private Warehouse warehouse;

    @BeforeEach
    void setup() {
        warehouse = getSavedWarehouse();
        manager = getSavedManager();
        forbiddenManager = getSavedManager();
        section = getSavedFreshSection(warehouse, manager);
        order = getSavedInboundOrder(section);
    }

    @Test
    void findBatchBySection_returnSectionBatches_whenGivenSectionCode() throws Exception {
        Product product = getSavedFreshProduct();
        Batch batch = getSavedBatch(product, order);

        Section newSection = getSavedFreshSection(warehouse, manager);
        InboundOrder newOrder = getSavedInboundOrder(newSection);
        getSavedBatch(product, newOrder);

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(hasSize(1)))
                .andExpect(jsonPath("$[0].batchNumber").value(batch.getBatchNumber()))
                .andExpect(jsonPath("$[0].productId").value(product.getProductId()))
                .andExpect(jsonPath("$[0].productName").value(product.getProductName()))
                .andExpect(jsonPath("$[0].productCategory").value(product.getCategory().toString()))
                .andExpect(jsonPath("$[0].dueDate").value(batch.getDueDate().format(dateFormatter)))
                .andExpect(jsonPath("$[0].currentQuantity").value(batch.getCurrentQuantity()));
    }

    @Test
    void findBatchBySection_returnSectionBatchesWithinDueDateInterval_whenGivenNumberOfDaysStartingNow() throws Exception {
        Product product = getSavedFreshProduct();
        Batch batch1 = getSavedBatch(LocalDate.now(), product, order);
        Batch batch2 = getSavedBatch(LocalDate.now().plusDays(10), product, order);
        getSavedBatch(LocalDate.now().plusDays(11), product, order);

        MvcResult result = mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ArrayNode node = (ArrayNode) objectMapper.readTree(result.getResponse().getContentAsString());

        long batchNumber1 = node.get(0).get("batchNumber").asLong();
        long batchNumber2 = node.get(1).get("batchNumber").asLong();

        assertThat(node.size()).isEqualTo(2);
        assertThat(batchNumber1).isNotEqualTo(batchNumber2);
        assertThat(batchNumber1).isIn(batch1.getBatchNumber(), batch2.getBatchNumber());
        assertThat(batchNumber2).isIn(batch1.getBatchNumber(), batch2.getBatchNumber());
    }

    @Test
    void findBatchBySection_returnError_whenGivenSectionCodeThatDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", "999")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findBatchBySection_returnError_whenGivenManagerThatDoesNotHavePermission() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", forbiddenManager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void findBatchBySection_returnError_whenGivenBadlyFormattedSectionCode() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", "abc")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchBySection_returnError_whenGivenBadlyFormattedNumberOfDays() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "abc")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchBySection_returnError_whenGivenManagerIdThatDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findBatchBySection_returnError_whenGivenBadlyFormattedManagerId() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchBySection_returnError_whenGivenNegativeNumberOfDays() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "-1")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchBySection_returnError_whenNotGivenManagerId() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("sectionCode", String.valueOf(section.getSectionCode()))
                        .queryParam("numberOfDays", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchByCategory_returnBatches_whenGivenCategory() throws Exception {
        Product product = getSavedFreshProduct();
        Batch batch = getSavedBatch(product, order);

        Section newSection = getSavedSection(warehouse, manager, Section.Category.CHILLED);
        InboundOrder newOrder = getSavedInboundOrder(newSection);
        Product newProduct = getSavedProduct(Section.Category.CHILLED);
        getSavedBatch(newProduct, newOrder);

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "ASC")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(hasSize(1)))
                .andExpect(jsonPath("$[0].batchNumber").value(batch.getBatchNumber()))
                .andExpect(jsonPath("$[0].productId").value(product.getProductId()))
                .andExpect(jsonPath("$[0].productName").value(product.getProductName()))
                .andExpect(jsonPath("$[0].productCategory").value(product.getCategory().toString()))
                .andExpect(jsonPath("$[0].dueDate").value(batch.getDueDate().format(dateFormatter)))
                .andExpect(jsonPath("$[0].currentQuantity").value(batch.getCurrentQuantity()));
    }

    @Test
    void findBatchByCategory_returnBatchesWithinTimeRange_whenGivenNumberOfDays() throws Exception {
        Product product = getSavedFreshProduct();
        Batch batch1 = getSavedBatch(LocalDate.now(), product, order);
        Batch batch2 = getSavedBatch(LocalDate.now().plusDays(10), product, order);
        getSavedBatch(LocalDate.now().plusDays(11), product, order);

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "ASC")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$[0].batchNumber").value(batch1.getBatchNumber()))
                .andExpect(jsonPath("$[1].batchNumber").value(batch2.getBatchNumber()));
    }

    @Test
    void findBatchByCategory_returnBatchesSortedInDescendingOrder_whenGivenOrderDirection() throws Exception {
        Product product = getSavedFreshProduct();
        Batch batch1 = getSavedBatch(LocalDate.now(), product, order);
        Batch batch2 = getSavedBatch(LocalDate.now().plusDays(10), product, order);
        getSavedBatch(LocalDate.now().plusDays(11), product, order);

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "desc")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$[1].batchNumber").value(batch1.getBatchNumber()))
                .andExpect(jsonPath("$[0].batchNumber").value(batch2.getBatchNumber()));
    }

    @Test
    void findBatchByCategory_returnError_whenGivenCategoryThatDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", "NAOEXISTE")
                        .queryParam("orderDir", "desc")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchByCategory_returnError_whenGivenInvalidOrderDirection() throws Exception {
        Product product = getSavedFreshProduct();

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "naotem")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchByCategory_returnError_whenGivenNegativeNumberOfDays() throws Exception {
        Product product = getSavedFreshProduct();

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "ASC")
                        .queryParam("numberOfDays", "-10")
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findBatchByCategory_returnError_whenGivenManagerIdThatDoesNotExist() throws Exception {
        Product product = getSavedFreshProduct();

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "ASC")
                        .queryParam("numberOfDays", "10")
                        .header("Manager-Id", 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findBatchByCategory_returnError_whenNotGivenManagerId() throws Exception {
        Product product = getSavedFreshProduct();

        mockMvc.perform(get("/api/v1/fresh-products/due-date")
                        .queryParam("category", product.getCategory().getCode())
                        .queryParam("orderDir", "ASC")
                        .queryParam("numberOfDays", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
