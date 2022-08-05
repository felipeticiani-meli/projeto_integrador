package com.mercadolibre.bootcamp.projeto_integrador.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BatchRequestDto {
    private long batchNumber;

    private long productId;

    private float currentTemperature;

    private float minimumTemperature;

    private int initialQuantity;

    private int currentQuantity;

    private LocalDate manufacturingDate;

    private LocalDateTime manufacturingTime;

    private LocalDate dueDate;

    private BigDecimal productPrice;
}
