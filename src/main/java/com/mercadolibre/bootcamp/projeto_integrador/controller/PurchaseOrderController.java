package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService service;

    @PostMapping("/fresh-products/orders")
    public ResponseEntity<BigDecimal> createPurchaseOrder(@RequestBody PurchaseOrderRequestDto purchaseOrder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(purchaseOrder));
    }
}
