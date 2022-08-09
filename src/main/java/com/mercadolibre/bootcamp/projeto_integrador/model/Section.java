package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sectionCode;

    @ManyToOne
    @JoinColumn(name = "warehouse_code")
    private Warehouse warehouse;

    @Column(columnDefinition = Category.mysqlDefinition)
    @Enumerated(EnumType.STRING)
    private Category category;

    private int maxBatches;

    private int currentBatches;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    public int getAvailableSlots() {
        return maxBatches - currentBatches;
    }

    public enum Category {
        FRESH,
        CHILLED,
        FROZEN;

        public static final String mysqlDefinition = "enum('FRESH', 'CHILLED', 'FROZEN')";
    }
}
