package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sectionCode;

    @ManyToOne
    @JoinColumn(name = "warehouse_code")
    @JsonIgnoreProperties("sections")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    private SectionCategory category;

    public enum SectionCategory {FRESH, CHILLED, FROZEN;}

    private int maxBatches;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties("sections")
    private Manager manager;
}
