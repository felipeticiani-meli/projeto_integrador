package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;

import java.time.LocalDate;
import java.util.List;

public class InboundOrderGenerator {
    public static InboundOrderRequestDto newInboundRequestDTO() {
        return InboundOrderRequestDto.builder()
                .sectionCode(1)
                .batchStock(List.of(BatchGenerator.newBatchRequestDTO()))
                .build();
    }

    public static InboundOrder newFreshInboundOrder() {
        return InboundOrder.builder()
                .orderNumber(1l)
                .orderDate(LocalDate.now().minusDays(1))
                .section(SectionGenerator.getFreshSectionWith10SlotsAvailable())
                .build();
    }
}
