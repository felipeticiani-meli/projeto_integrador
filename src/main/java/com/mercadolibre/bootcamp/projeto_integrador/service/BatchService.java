package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BatchService implements IBatchService{

    @Autowired
    IBatchRepository batchRepository;

    @Override
    public Batch update(Batch batch) {
        //TODO: BatchNotFound
        Batch b = batchRepository.findById(batch.getBatchNumber())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        batch.setCurrentQuantity(batch.getInitialQuantity() - (b.getInitialQuantity() - b.getCurrentQuantity()));
        if(batch.getCurrentQuantity() < 0){
            //TODO: InvalidMaxQuantity
            throw new RuntimeException("Current quantity invalid");
        }
        batchRepository.save(batch);
        return batch;
    }
}
