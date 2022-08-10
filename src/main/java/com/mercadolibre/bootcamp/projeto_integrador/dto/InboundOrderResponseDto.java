package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class InboundOrderResponseDto {
    @JsonIgnoreProperties("inboundOrder")
    private List<Batch> batchStock;
}
