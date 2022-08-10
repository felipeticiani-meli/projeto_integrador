package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;

public class WarehouseGenerator {
    public static Warehouse newWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setLocation("New York");
        return warehouse;
    }
}
