package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;

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
}
