package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.InboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class InboundOrderController {

    @Autowired
    private InboundOrderService service;

    @PostMapping("/fresh-products/inboundorder")
    public ResponseEntity<InboundOrderResponseDto> createInboundOrder(@RequestBody InboundOrderRequestDto inboundOrder) {
        return ResponseEntity.ok(service.create(inboundOrder));
    }

    @PutMapping("/fresh-products/inboundorder")
    public ResponseEntity<InboundOrderResponseDto> updateInboundOrder(@RequestBody InboundOrderRequestDto inboundOrder) {
        return ResponseEntity.ok(service.update(inboundOrder));
    }
}
