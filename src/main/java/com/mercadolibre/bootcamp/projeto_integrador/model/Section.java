package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
        FRESH("FS"),
        CHILLED("RF"),
        FROZEN("FF");

        public static final String mysqlDefinition = "enum('FRESH', 'CHILLED', 'FROZEN')";

        @Getter
        private final String code;

        Category(String code) {
            this.code = code;
        }
    }
}
