package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
class CreatePurchaseOrderTest extends BaseControllerTest {
    private Buyer buyer;
    private Batch batch;

    private PurchaseOrderRequestDto validPurchaseOrderRequest;
    private PurchaseOrderRequestDto purchaseOrderWithExpiredBatchRequest;

    @BeforeEach
    void setup() {

        buyer = getSavedBuyer();
        Warehouse warehouse = getSavedWarehouse();

        Manager manager = getSavedManager();

        Section section = getSavedFreshSection(warehouse, manager);
        InboundOrder inboundOrder = getSavedInboundOrder(section);
        Product product = getSavedFreshProduct();

        batch = getSavedValidBatch(product, inboundOrder);
        Batch expiredBatch = getSavedBatch(product, inboundOrder);

        validPurchaseOrderRequest = getPurchaseOrderRequestDto(getValidBatchPurchaseOrderRequestDto(batch));
        purchaseOrderWithExpiredBatchRequest = getPurchaseOrderRequestDto(getValidBatchPurchaseOrderRequestDto(expiredBatch));
    }

    @Test
    void createPurchaseOrder_returnsCreated_whenIsGivenANewPurchaseOrder() throws Exception {
        int quantityPurchaseOrder = purchaseOrderRepository.findAll().size();
        int quantityBatchPurchaseOrder = batchPurchaseOrderRepository.findAll().size();

        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(purchaseOrderRepository.findAll().size()).isEqualTo(quantityPurchaseOrder + 1);
        assertThat(batchPurchaseOrderRepository.findAll().size()).isEqualTo(quantityBatchPurchaseOrder + 1);
        assertThat(batchRepository.findById(batch.getBatchNumber()).get().getCurrentQuantity()).isEqualTo(14);

    }

    @Test
    void createPurchaseOrder_returnsCreated_whenIsGivenAnExistentPurchaseOrder() throws Exception {

        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        int quantityPurchaseOrder = purchaseOrderRepository.findAll().size();
        int quantityBatchPurchaseOrder = batchPurchaseOrderRepository.findAll().size();

        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(purchaseOrderRepository.findAll().size()).isEqualTo(quantityPurchaseOrder);
        assertThat(batchPurchaseOrderRepository.findAll().size()).isEqualTo(quantityBatchPurchaseOrder);
        assertThat(batchRepository.findById(batch.getBatchNumber()).get().getCurrentQuantity()).isEqualTo(13);
    }

    @Test
    void createPurchaseOrder_returnsNotFound_whenIsGivenAnInvalidBuyer() throws Exception {

        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", 2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void createPurchaseOrder_returnsNotFound_whenIsGivenABatchWithAnExpiredProduct() throws Exception {

        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(purchaseOrderWithExpiredBatchRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}