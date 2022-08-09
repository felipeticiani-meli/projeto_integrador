package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBuyerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IPurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PurchaseOrder implements IPurchaseOrder {

    @Autowired
    IBuyerRepository buyerRepository;

    @Autowired
    IProductRepository productRepository;

    @Autowired
    IPurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    IBatchRepository batchRepository;

    @Override
    public PurchaseOrder create(PurchaseOrderRequestDto request) {
        float totalPrice;
        findBuyer(request.getBuyerId());
        // verifica se o produto está no cadastro
        List<Batch> foundBatches = request.getProducts()
                .stream()
                .map((product) -> {
                    findProductById(product.getProductId());
                    return batchRepository.findByProductId(product.getProductId());
                })
                .collect(Collectors.toList());

        return null;
        // salvar - vem do body
//        request.getProducts()
//                .stream()
//                .map((product) -> checkQuantityAndDueDate(foundBatches, product, request.isFinished()))


    }

    @Override
    public PurchaseOrder update(PurchaseOrderRequestDto request) {
        return null;
    }

    public void findBuyer(long buyerId) {
        Optional<Buyer> foundBuyer = buyerRepository.findById(buyerId);
        // TODO: inserir NOT FOUND EXCEPTION
        if(foundBuyer.isEmpty()) throw new RuntimeException("Buyer not found");
    }

    public void findProductById(long productId) {
        // TODO: inserir NOT FOUND EXCEPTION
        Optional<Product> foundProduct = productRepository.findById(productId);
        if(foundProduct.isEmpty()) throw new RuntimeException("Product not found");

    }

    public void checkQuantityAndDueDate(List<Batch> batches, ProductDto product, boolean isFinished) {
        Batch batchProduct = batches.stream()
                .filter((batch) -> batch.getProduct().getProductId() == product.getProductId())
                .filter((batch) -> {
                    try {
                        return batch.getDueDate().minus(21, ChronoUnit.DAYS).compareTo(LocalDate.now()) > 0;
                    } catch(RuntimeException ex) {
                        throw new RuntimeException("The due date has passed");
                    }
                })
                .filter((batch) -> {
                    try {
                        return batch.getCurrentQuantity() >= product.getQuantity();
                    } catch(RuntimeException ex) {
                        throw new RuntimeException("There is not enough quantity");
                    }
                })
                .findFirst().get();

        if (isFinished) {
            int result = batchProduct.getCurrentQuantity() - product.getQuantity();
            batchProduct.setCurrentQuantity(result);
        }
    }
}