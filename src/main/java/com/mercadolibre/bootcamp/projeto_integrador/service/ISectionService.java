package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;

import java.util.List;

public interface ISectionService {
    Section findById(long sectionCode);

    Section update(Section section, List<BatchRequestDto> batchesToInsert, long managerId);
}
