package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.dto.WarehouseResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;

import java.util.ArrayList;
import java.util.List;

public class WarehouseGenerator {
    public static Warehouse newWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(1);
        warehouse.setLocation("New York");
        return warehouse;
    }

    public static Warehouse newWarehouseWithoutCode() {
        Warehouse warehouse = new Warehouse();
        warehouse.setLocation("New York");
        return warehouse;
    }

    public static WarehouseResponseDto newWarehouseResponseDto() {
        WarehouseResponseDto warehouse = new WarehouseResponseDto(1, 50);
        return warehouse;
    }

    public static List<WarehouseResponseDto> newListWarehouseResponseDto() {
        List<WarehouseResponseDto> warehouses = new ArrayList<>();
        warehouses.add(newWarehouseResponseDto());
        warehouses.add(newWarehouseResponseDto());
        warehouses.add(newWarehouseResponseDto());
        warehouses.get(1).setWarehouseCode(2);
        warehouses.get(1).setTotalQuantity(37);
        warehouses.get(2).setWarehouseCode(3);
        warehouses.get(2).setTotalQuantity(121);
        return warehouses;
    }
}
