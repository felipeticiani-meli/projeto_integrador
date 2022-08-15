package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;

public class ManagerGenerator {
    public static Manager newManager() {
        Manager manager = new Manager();
        manager.setName("John Doe");
        manager.setUsername("john");
        manager.setEmail("john@example.com");
        return manager;
    }

    public static Manager getManagerWithId(long id) {
        Manager manager = new Manager();
        manager.setName("John Doe");
        manager.setUsername("john");
        manager.setEmail("john@example.com");
        manager.setManagerId(id);
        return manager;
    }

    public static Manager getManagerWithId() {
        return getManagerWithId(1L);
    }
}
