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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class UpdatePurchaseOrderTest extends BaseControllerTest {
    private Buyer buyer;

    private PurchaseOrderRequestDto validPurchaseOrderRequest;

    @BeforeEach
    void setup() {

        buyer = getSavedBuyer();
        Warehouse warehouse = getSavedWarehouse();

        Manager manager = getSavedManager();

        Section section = getSavedFreshSection(warehouse, manager);
        InboundOrder inboundOrder = getSavedInboundOrder(section);
        Product product = getSavedFreshProduct();

        Batch batch = getSavedValidBatch(product, inboundOrder);

        validPurchaseOrderRequest = getPurchaseOrderRequestDto(getValidBatchPurchaseOrderRequestDto(batch));
    }

    @Test
    void updatePurchaseOrder_returnsOk_whenIsGivenAPurchaseOrderUpdate() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, "Opened");
        int quantityPurchaseOrder = purchaseOrderRepository.findAll().size();
        int quantityBatchPurchaseOrder = batchPurchaseOrderRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/orders")
                .param("purchaseOrderId","" + purchaseOrder.getPurchaseId())
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        PurchaseOrder updatedPurchaseOrder =  purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, "Closed");

        assertThat(purchaseOrderRepository.findAll().size()).isEqualTo(quantityPurchaseOrder);
        assertThat(batchPurchaseOrderRepository.findAll().size()).isEqualTo(quantityBatchPurchaseOrder);
        assertThat(updatedPurchaseOrder.getOrderStatus()).isEqualTo("Closed");
    }

    @Test
    void updatePurchaseOrder_returnsNotFound_whenIsGivenAInvalidPurchaseOrder() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, "Opened");
        purchaseOrder.setPurchaseId(2);
        int quantityPurchaseOrder = purchaseOrderRepository.findAll().size();
        int quantityBatchPurchaseOrder = batchPurchaseOrderRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/orders")
                .param("purchaseOrderId","" + purchaseOrder.getPurchaseId())
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        assertThat(purchaseOrderRepository.findAll().size()).isEqualTo(quantityPurchaseOrder);
        assertThat(batchPurchaseOrderRepository.findAll().size()).isEqualTo(quantityBatchPurchaseOrder);
    }

    @Test
    void updatePurchaseOrder_returnsNotFound_whenIsGivenAInvalidBuyer() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, "Opened");
        int quantityPurchaseOrder = purchaseOrderRepository.findAll().size();
        int quantityBatchPurchaseOrder = batchPurchaseOrderRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/orders")
                .param("purchaseOrderId","" + purchaseOrder.getPurchaseId())
                .header("Buyer-Id", 2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        assertThat(purchaseOrderRepository.findAll().size()).isEqualTo(quantityPurchaseOrder);
        assertThat(batchPurchaseOrderRepository.findAll().size()).isEqualTo(quantityBatchPurchaseOrder);
    }

    @Test
    void updatePurchaseOrder_returnsNotFound_whenIsGivenAClosedPurchaseOrder() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(validPurchaseOrderRequest))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, "Opened");
        purchaseOrder.setOrderStatus("Closed");
        purchaseOrderRepository.save(purchaseOrder);
        int quantityPurchaseOrder = purchaseOrderRepository.findAll().size();
        int quantityBatchPurchaseOrder = batchPurchaseOrderRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/orders")
                .param("purchaseOrderId","" + purchaseOrder.getPurchaseId())
                .header("Buyer-Id", "" + buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertThat(purchaseOrderRepository.findAll().size()).isEqualTo(quantityPurchaseOrder);
        assertThat(batchPurchaseOrderRepository.findAll().size()).isEqualTo(quantityBatchPurchaseOrder);
    }
}
