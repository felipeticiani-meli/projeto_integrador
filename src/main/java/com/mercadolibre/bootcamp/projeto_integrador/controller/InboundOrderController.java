package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.IInboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class InboundOrderController {

    @Autowired
    private IInboundOrderService service;

    @PostMapping("/fresh-products/inboundorder")
    public ResponseEntity<InboundOrderResponseDto> createInboundOrder(@RequestBody @Valid InboundOrderRequestDto inboundOrder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(inboundOrder));
    }

    @PutMapping("/fresh-products/inboundorder")
    public ResponseEntity<InboundOrderResponseDto> updateInboundOrder(@RequestParam long orderNumber,  @RequestBody @Valid InboundOrderRequestDto inboundOrder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.update(orderNumber, inboundOrder));
    }
}
