package com.mercadolibre.bootcamp.projeto_integrador.repository;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.BatchPurchaseOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IBatchPurchaseOrderRepository extends JpaRepository<BatchPurchaseOrder, Long> {
    Optional<BatchPurchaseOrder> findOneByPurchaseOrderAndBatch(PurchaseOrder purchaseOrder, Batch batch);
    Optional<BatchPurchaseOrder> findOneByPurchaseOrderAndBatch_Product(PurchaseOrder purchase, Product product);
}
