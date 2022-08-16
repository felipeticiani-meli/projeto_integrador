package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import lombok.Data;

@Data
public class SectionResponseDto {
    private long sectionCode;
    private long warehouseCode;

    public SectionResponseDto(Section section) {
        setSectionCode(section.getSectionCode());
        setWarehouseCode(section.getWarehouse().getWarehouseCode());
    }
}
