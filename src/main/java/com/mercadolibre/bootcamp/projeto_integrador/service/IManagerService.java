package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;

public interface IManagerService {
    Manager findById(long managerId);
}
