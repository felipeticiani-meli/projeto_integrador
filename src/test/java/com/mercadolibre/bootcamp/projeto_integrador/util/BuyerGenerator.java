package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;

public class BuyerGenerator {
    public static Buyer newBuyer() {
        Buyer buyer = new Buyer();
        buyer.setUsername("carol");
        return buyer;
    }

    public static Buyer getBuyerWithId() {
        Buyer buyer = new Buyer();
        buyer.setUsername("carol");
        buyer.setBuyerId(1l);
        return buyer;
    }

    public static Buyer buyerWithoutPurchase() {
        Buyer buyer = new Buyer();
        buyer.setUsername("emerson");
        return buyer;
    }
}
