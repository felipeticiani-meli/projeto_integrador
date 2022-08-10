package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequestDto {

    private long batchNumber;

    @NotNull(message = "O Id do produto deve ser informado")
    @Positive(message = "O Id do produto deve ser um número positivo")
    private long productId;

    @NotNull(message = "A temperatura atual deve ser informada")
    private float currentTemperature;

    @NotNull(message = "A temperatura mínima dever ser informada")
    private float minimumTemperature;

    @NotNull(message = "A quantidade inicial deve ser informada")
    @Min(value = 0, message = "A quantidade inicial deve ser maior ou igual a 0")
    private int initialQuantity;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "A data de fabricação deve ser informada")
    @PastOrPresent(message = "A date de fabricação deve ser menor ou igual a data atual")
    private LocalDate manufacturingDate;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @NotNull(message = "A hora de fabricação deve ser informada")
    @PastOrPresent(message = "A hora de fabricação deve ser menor ou igual a data atual")
    private LocalDateTime manufacturingTime;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "A data de vencimento deve ser informada")
    @Future(message = "A data de vencimento deve ser posterior a data atual")
    private LocalDate dueDate;

    @NotNull(message = "O preço do produto deve ser informado")
    @PositiveOrZero(message = "O preço não pode ser negativo")
    private BigDecimal productPrice;
}
