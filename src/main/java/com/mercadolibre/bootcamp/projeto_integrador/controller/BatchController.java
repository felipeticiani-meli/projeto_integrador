package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchDueDateResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BatchController {
    @Autowired
    private IBatchService batchService;

    @GetMapping(value = "/fresh-products/due-date", params = "sectionCode")
    public ResponseEntity<List<BatchDueDateResponseDto>> findBatchBySection(long sectionCode,
                                                                            @RequestHeader("Manager-Id") long managerId) {
        return ResponseEntity.ok(batchService.findBatchBySection(sectionCode, managerId));
    }

    @GetMapping(value = "/fresh-products/due-date", params = {"category", "numberOfDays"})
    public ResponseEntity<List<BatchDueDateResponseDto>> findBatchByCategory(String category,
                                                                             int numberOfDays,
                                                                             @RequestHeader("Manager-Id") long managerId) {
        return ResponseEntity.ok(batchService.findBatchByCategoryAndDueDate(category, numberOfDays, managerId));
    }
}
