package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;

public interface IPurchaseOrder {
    PurchaseOrder create (PurchaseOrderRequestDto request);
    PurchaseOrder update (PurchaseOrderRequestDto request);
}
