package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;

import java.math.BigDecimal;
import java.util.List;

public interface IPurchaseOrderService {
    BigDecimal create (PurchaseOrderRequestDto request);
    BigDecimal update (long purchaseOrderId);
    void dropProducts(long purchaseOrderId, List<ProductDto> productsDto);
}
