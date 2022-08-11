package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService service;

    @PostMapping("/fresh-products/orders")
    public ResponseEntity<BigDecimal> createPurchaseOrder(@RequestBody @Valid PurchaseOrderRequestDto purchaseOrder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(purchaseOrder));
    }

    @PutMapping("/fresh-products/orders")
    public ResponseEntity<BigDecimal> updatePurchaseOrder(@RequestParam long purchaseOrderId) {
        return ResponseEntity.ok(service.update(purchaseOrderId));
    }

    @DeleteMapping("/fresh-products/orders")
    public ResponseEntity<Void> dropProductsPurchaseOrder(@RequestParam long purchaseOrderId, @RequestBody List<ProductDto> productsDto) {
        service.dropProducts(purchaseOrderId, productsDto);
        return ResponseEntity.noContent().build();
    }
}
