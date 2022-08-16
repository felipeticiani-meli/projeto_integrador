package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.exceptions.ManagerNotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerService implements IManagerService {
    @Autowired
    private IManagerRepository managerRepository;

    @Override
    public Manager findById(long managerId) {
        return managerRepository.findById(managerId).orElseThrow(() -> new ManagerNotFoundException(managerId));
    }
}
