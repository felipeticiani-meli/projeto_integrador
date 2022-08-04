package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long managerId;

    @Column(length = 100)
    private String name;

    @Column(length = 45)
    private String username;

    @Column(length = 60)
    private String email;

    @OneToMany(mappedBy = "manager")
    @JsonIgnoreProperties("manager")
    private List<Section> sections;
}
