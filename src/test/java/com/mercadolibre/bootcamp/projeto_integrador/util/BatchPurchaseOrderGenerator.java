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
}
