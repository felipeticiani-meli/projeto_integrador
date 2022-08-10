package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;

import java.util.List;

public class InboundOrderGenerator {
    public static InboundOrderRequestDto newInboundRequestDTO() {
        return InboundOrderRequestDto.builder()
                .sectionCode(1)
                .batchStock(List.of(BatchGenerator.newBatchRequestDTO()))
                .build();
    }
}
