package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;

import java.math.BigDecimal;
import java.util.List;

public interface IPurchaseOrderService {
    BigDecimal create (PurchaseOrderRequestDto request, long buyerId);
    BigDecimal update (long purchaseOrderId, long buyerId);
    void dropProducts(long purchaseOrderId, BatchPurchaseOrderRequestDto batchDto, long buyerId);
}
