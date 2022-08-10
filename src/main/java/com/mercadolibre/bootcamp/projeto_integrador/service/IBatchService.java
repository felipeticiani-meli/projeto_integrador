package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;

import java.util.List;

public interface IBatchService {
    Batch update(InboundOrder order, Batch batch);

    List<BatchBuyerResponseDto> findAll();

    List<BatchBuyerResponseDto> findBatchByCategory(String categoryCode);
}
