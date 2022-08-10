package com.mercadolibre.bootcamp.projeto_integrador.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Buyer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long buyerId;

    @Column(length = 45)
    private String username;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders;
}