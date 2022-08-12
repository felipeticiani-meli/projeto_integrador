package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchDueDateResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;

import java.util.List;
import java.util.Map;

public interface IBatchService {
    List<Batch> updateAll(InboundOrder order, List<BatchRequestDto> batchesDto, Map<Long, Product> products);

    @Deprecated
    Batch update(InboundOrder order, Batch batch);

    List<BatchBuyerResponseDto> findAll();

    List<BatchBuyerResponseDto> findBatchByCategory(String categoryCode);

    List<BatchDueDateResponseDto> findBatchBySection(long sectionCode, int numberOfDays, long managerId);

    List<BatchDueDateResponseDto> findBatchByCategoryAndDueDate(String categoryCode, int numberOfDays, String orderDir, long managerId);
}
