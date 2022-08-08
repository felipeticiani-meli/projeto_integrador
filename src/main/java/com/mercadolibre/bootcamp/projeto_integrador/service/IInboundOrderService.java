package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import org.springframework.transaction.annotation.Transactional;

public interface IInboundOrderService {
    @Transactional
    InboundOrderResponseDto create(InboundOrderRequestDto request, long managerId);

    @Transactional
    InboundOrderResponseDto update(long orderNumber, InboundOrderRequestDto request, long managerId);
}
