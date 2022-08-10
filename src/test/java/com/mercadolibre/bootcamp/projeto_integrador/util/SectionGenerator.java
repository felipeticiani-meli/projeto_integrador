package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;

public class SectionGenerator {
    public static Section getSection(Warehouse warehouse, Manager manager) {
        Section section = new Section();
        section.setCurrentBatches(1);
        section.setCategory(Section.Category.FRESH);
        section.setWarehouse(warehouse);
        section.setManager(manager);
        section.setMaxBatches(10);
        return section;
    }

    public static Section getSectionWith1SlotAvailable() {
        return Section.builder()
                .sectionCode(1)
                .category(Section.Category.FRESH)
                .maxBatches(8)
                .currentBatches(7)
                .manager(ManagerGenerator.getManagerWithId())
                .build();
    }

    public static Section getSectionWith10SlotsAvailable() {
        return Section.builder()
                .sectionCode(2)
                .category(Section.Category.FRESH)
                .maxBatches(10)
                .currentBatches(0)
                .manager(ManagerGenerator.getManagerWithId())
                .build();
    }

    public static Section getCrowdedSection() {
        return Section.builder()
                .sectionCode(3)
                .category(Section.Category.FRESH)
                .maxBatches(20)
                .currentBatches(20)
                .manager(ManagerGenerator.getManagerWithId())
                .build();
    }
}