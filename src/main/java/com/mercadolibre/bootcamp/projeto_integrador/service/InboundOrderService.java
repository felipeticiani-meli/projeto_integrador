package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IInboundOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InboundOrderService implements IInboundOrderService {

    @Autowired
    private IBatchService batchService;

    @Autowired
    private IInboundOrderRepository inboundOrderRepository;

    @Autowired
    private ISectionService sectionService;

    /**
     * Método que faz a criação da InboundOrder com novos lotes
     * @param request InboundOrderRequestDto
     * @return InboundOrderResponseDto contendo os dados dos lotes inseridos
     */
    @Override
    @Transactional
    public InboundOrderResponseDto create(InboundOrderRequestDto request, long managerId) {
        Section section = sectionService.findById(request.getSectionCode());
        sectionService.update(section, request.getBatchStock(), managerId);

        InboundOrder order = new InboundOrder();
        order.setSection(section);
        order.setOrderDate(LocalDate.now());

        inboundOrderRepository.save(order);

        List<Batch> batches = batchService.createAll(request.getBatchStock(), order);

        return new InboundOrderResponseDto(batches);
    }

    /**
     * Metodo que faz a atualização da InboundOrder, com novos lotes ou atualiza os que já estão dentro
     * @param orderNumber long representando o id da InboundOrder
     * @param request objeto InboundOrderRequestDto
     * @return InboundOrderResponseDto contendo as infos dos lotes atualizados/inseridos
     */
    @Override
    @Transactional
    public InboundOrderResponseDto update(long orderNumber, InboundOrderRequestDto request, long managerId) {
        InboundOrder order = inboundOrderRepository.findById(orderNumber)
                .orElseThrow(() -> new NotFoundException("Inbound Order"));

        List<BatchRequestDto> batchesDto = request.getBatchStock();
        List<BatchRequestDto> batchesToInsert = batchesDto.stream()
                .filter(b -> b.getBatchNumber() == 0L)
                .collect(Collectors.toList());

        sectionService.update(order.getSection(), batchesToInsert, managerId);
        List<Batch> savedBatches = batchService.updateAll(order, batchesDto);

        return new InboundOrderResponseDto(savedBatches);
    }
}
