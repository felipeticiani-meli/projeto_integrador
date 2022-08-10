package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;

public class ProductsGenerator {
    public static Product newProductFresh() {
        return Product.builder()
                .productName("Maça")
                .brand("Nacional")
                .category(Section.Category.FRESH)
                .build();
    }

    public static Product newProductChilled() {
        return Product.builder()
                .productName("Iogurte")
                .brand("Holandês")
                .category(Section.Category.CHILLED)
                .build();
    }

    public static Product newProductFrozen() {
        return Product.builder()
                .productName("Açaí")
                .brand("Frooty")
                .category(Section.Category.FROZEN)
                .build();
    }
}
