package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;

import java.math.BigDecimal;

public interface IPurchaseOrderService {
    BigDecimal create (PurchaseOrderRequestDto request);
    BigDecimal update (long purchaseOrderId, PurchaseOrderRequestDto request);
}
