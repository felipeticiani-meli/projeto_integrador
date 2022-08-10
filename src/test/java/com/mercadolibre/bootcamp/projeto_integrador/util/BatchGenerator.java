package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BatchGenerator {
    public static BatchRequestDto newBatchRequestDTO() {
        return BatchRequestDto.builder()
                .productId(1)
                .currentTemperature(15.0f)
                .minimumTemperature(5.0f)
                .initialQuantity(15)
                .manufacturingDate(LocalDate.now().minusDays(2))
                .manufacturingTime(LocalDateTime.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(30))
                .productPrice(new BigDecimal("5.9"))
                .build();
    }

    public static List<BatchRequestDto> newList2BatchRequestsDTO() {
        List<BatchRequestDto> batchRequests = new ArrayList<>();
        batchRequests.add(BatchRequestDto.builder()
                .productId(2)
                .currentTemperature(-5.0f)
                .minimumTemperature(-15.0f)
                .initialQuantity(50)
                .manufacturingDate(LocalDate.now().minusDays(2))
                .manufacturingTime(LocalDateTime.now().minusDays(2))
                .dueDate(LocalDate.now().plusWeeks(3))
                .productPrice(new BigDecimal("5.9"))
                .build()
        );
        batchRequests.add(BatchRequestDto.builder()
                .productId(3)
                .currentTemperature(-5.0f)
                .minimumTemperature(-15.0f)
                .initialQuantity(20)
                .manufacturingDate(LocalDate.now().minusDays(2))
                .manufacturingTime(LocalDateTime.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(10))
                .productPrice(new BigDecimal("2.49"))
                .build()
        );

        return batchRequests;
    }

    public static List<Batch> newBatchList() {
        List<Batch> batches = new ArrayList<>();
        batches.add(Batch.builder()
                .batchNumber(1L)
                .product(ProductsGenerator.newProductFresh())
                .currentTemperature(18.7f)
                .minimumTemperature(10.0f)
                .initialQuantity(30)
                .currentQuantity(30)
                .manufacturingDate(LocalDate.now().minusDays(7))
                .manufacturingTime(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDate.now().plusWeeks(3))
                .inboundOrder(InboundOrderGenerator.newInboundOrder())
                .productPrice(BigDecimal.valueOf(10.99))
                .build());
        batches.get(0).getInboundOrder().getSection().setSectionCode(5);
        batches.get(0).getInboundOrder().getSection().getManager().setManagerId(2);
        batches.add(Batch.builder()
                .batchNumber(2L)
                .product(ProductsGenerator.newProductFresh())
                .currentTemperature(18.7f)
                .minimumTemperature(10.0f)
                .initialQuantity(20)
                .currentQuantity(20)
                .manufacturingDate(LocalDate.now().minusDays(7))
                .manufacturingTime(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDate.now().plusWeeks(2))
                .inboundOrder(InboundOrderGenerator.newInboundOrder())
                .productPrice(BigDecimal.valueOf(10.99))
                .build());
        batches.get(1).getInboundOrder().getSection().setSectionCode(8);
        batches.get(1).getInboundOrder().getSection().getManager().setManagerId(2);
        batches.add(Batch.builder()
                .batchNumber(3L)
                .product(ProductsGenerator.newProductFresh())
                .currentTemperature(18.7f)
                .minimumTemperature(10.0f)
                .initialQuantity(40)
                .currentQuantity(40)
                .manufacturingDate(LocalDate.now().minusDays(7))
                .manufacturingTime(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDate.now().plusWeeks(1))
                .inboundOrder(InboundOrderGenerator.newInboundOrder())
                .productPrice(BigDecimal.valueOf(10.99))
                .build());
        batches.get(2).getInboundOrder().getSection().setSectionCode(2);
        batches.get(2).getInboundOrder().getSection().getManager().setManagerId(2);
        return batches;
    }
}
