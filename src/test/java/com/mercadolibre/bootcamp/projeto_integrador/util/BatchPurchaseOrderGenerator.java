package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.BatchPurchaseOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.PurchaseOrder;

import java.util.ArrayList;
import java.util.List;

public class BatchPurchaseOrderGenerator {

    public static BatchPurchaseOrderRequestDto newBatchPurchaseOrderRequestDto(long batchNumber) {
        return BatchPurchaseOrderRequestDto.builder()
                .batchNumber(batchNumber)
                .quantity(1)
                .build();
    }

//    public static BatchPurchaseOrder newBatchPurchaseOrder(PurchaseOrder purchaseOrder, Batch batch) {
//        return BatchPurchaseOrder.builder()
//                .batchPurchaseId(1l)
//                .purchaseOrder(purchaseOrder)
//                .batch(batch)
//                .unitPrice(batch.getProductPrice())
//                .quantity(batch.getCurrentQuantity())
//                .build();
//    }

//    public static List<BatchPurchaseOrder> newBatchPurchaseOrderList(PurchaseOrder purchaseOrder, Batch batch) {
//        List<BatchPurchaseOrder> batchPurchaseOrderList = new ArrayList<>();
//        batchPurchaseOrderList.add(BatchPurchaseOrder.builder()
//                .batchPurchaseId(1)
//                .purchaseOrder(purchaseOrder)
//                .batch(batch)
//                .unitPrice(batch.getProductPrice())
//                .quantity(batch.getCurrentQuantity())
//                .build());
//        return batchPurchaseOrderList;
//    }
}
