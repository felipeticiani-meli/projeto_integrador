package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fresh-products")
public class FreshProductsController {
    @Autowired
    IBatchService batchService;

    @GetMapping
    public ResponseEntity<List<BatchBuyerResponseDto>> findBatches(@RequestParam(required = false) String category) {
        return category != null
                ? ResponseEntity.ok(batchService.findBatchByCategory(category))
                : ResponseEntity.ok(batchService.findAll());
    }
}
