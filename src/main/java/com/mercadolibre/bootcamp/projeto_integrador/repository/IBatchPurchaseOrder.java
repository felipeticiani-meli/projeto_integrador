package com.mercadolibre.bootcamp.projeto_integrador.repository;

import com.mercadolibre.bootcamp.projeto_integrador.model.BatchPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBatchPurchaseOrder extends JpaRepository<BatchPurchaseOrder, Long> {
}
