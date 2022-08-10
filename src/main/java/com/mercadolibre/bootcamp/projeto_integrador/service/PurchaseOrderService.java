package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.PurchaseOrder;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService implements IPurchaseOrderService {

    @Autowired
    IBuyerRepository buyerRepository;

    @Autowired
    IProductRepository productRepository;

    @Autowired
    IPurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IBatchPurchaseOrderRepository batchPurchaseOrderRepository;

    @Override
    public BigDecimal create(PurchaseOrderRequestDto request) {
         findBuyer(request.getBuyerId());
        // verifica se o produto está no cadastro
        List<Batch> foundBatches = request.getProducts()
                .stream()
                .map((product) -> {
                    Product p = findProductById(product.getProductId());
                    return checkQuantityAndDueDate(batchRepository.findByProduct(p), product, request.getOrderStatus());
                })
                .collect(Collectors.toList());

        // calcula preço total da compra
        BigDecimal totalPrice = request.getProducts()
                .stream()
                .map(product -> sumTotalPrice(foundBatches, product))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PurchaseOrder purchase = new PurchaseOrder(request);
        purchase.setBuyer(buyerRepository.findById(request.getBuyerId()).get());
        purchaseOrderRepository.save(purchase);
        return totalPrice;
    }

    @Override
    public PurchaseOrderService update(PurchaseOrderRequestDto request) {
        return null;
    }

    public void findBuyer(long buyerId) {
        Optional<Buyer> foundBuyer = buyerRepository.findById(buyerId);
        // TODO: inserir NOT FOUND EXCEPTION
        if(foundBuyer.isEmpty()) throw new RuntimeException("Buyer not found" + buyerId);

    }

    public Product findProductById(long productId) {
        // TODO: inserir NOT FOUND EXCEPTION
        Optional<Product> foundProduct = productRepository.findById(productId);
        if(foundProduct.isEmpty()) throw new RuntimeException("Product not found");
        return foundProduct.get();
    }

    public Batch checkQuantityAndDueDate(List<Batch> batches, ProductDto product, String orderStatus) {
        Batch batchProduct = batches.stream()
                .filter((batch) -> batch.getProduct().getProductId() == product.getProductId())
                .filter((batch) -> batch.getDueDate().minus(21, ChronoUnit.DAYS).compareTo(LocalDate.now()) > 0)
                .filter((batch) -> batch.getCurrentQuantity() >= product.getQuantity())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("There are no available products"));

        if (orderStatus.equals("Closed")) {
            int result = batchProduct.getCurrentQuantity() - product.getQuantity();
            batchProduct.setCurrentQuantity(result);
        }

        return batchProduct;
    }

    public BigDecimal sumTotalPrice(List<Batch> batches, ProductDto product) {
        return batches
                .stream().filter(b -> b.getProduct().getProductId() == product.getProductId())
                .map(batch -> batch.getProductPrice().multiply(new BigDecimal(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}