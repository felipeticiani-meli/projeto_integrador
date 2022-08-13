package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;

import java.util.List;

public interface IBatchService {
    List<Batch> createAll(List<BatchRequestDto> batchesDto, InboundOrder order);

    List<Batch> updateAll(InboundOrder order, List<BatchRequestDto> batchesDto);

    @Deprecated
    Batch update(InboundOrder order, Batch batch);

    List<BatchBuyerResponseDto> findAll();

    List<BatchBuyerResponseDto> findBatchByCategory(String categoryCode);
}
