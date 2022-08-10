package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.ProductOutOfStockException;
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
        // verifica se o produto está no cadastro
        List<Batch> foundBatches = foundBatches(request.getProducts(), request.getOrderStatus());

        BigDecimal totalPrice = sumTotalPrice(foundBatches, request.getProducts());

        PurchaseOrder purchase = new PurchaseOrder(request);
        purchase.setBuyer(findBuyer(request.getBuyerId()));
        purchase.setDate(LocalDate.now());
        purchaseOrderRepository.save(purchase);
        return totalPrice;
    }

    @Override
    public BigDecimal update(long purchaseOrderId, PurchaseOrderRequestDto request) {
        PurchaseOrder foundOrder = findOrder(purchaseOrderId);
        if (!(foundOrder.getBuyer().getBuyerId() == request.getBuyerId())) {
            throw new RuntimeException("Wrong BuyerId");
        }
        if (foundOrder.getOrderStatus().equals("Closed")) {
            throw new RuntimeException("Can't update a closed order");
        }
    return null;
    }

    public List<Batch> foundBatches(List<ProductDto> products, String orderStatus) {
        return products.stream()
                .map((product) -> {
                    Product p = findProductById(product.getProductId());
                    return checkQuantityAndDueDate(batchRepository.findByProduct(p), product, orderStatus);
                })
                .collect(Collectors.toList());
    }

    public PurchaseOrder findOrder(long purchaseOrderId) {
        Optional<PurchaseOrder> foundOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if(foundOrder.isEmpty()) throw new RuntimeException("Purchase order not found");
        return foundOrder.get();
    }

    /**
     * Metodo que verifica se comprador existe e o retorna.
     * @param buyerId identificador do comprador.
     * @return Objeto Buyer contendo infos do comprador.
     */
    private Buyer findBuyer(long buyerId) {
        Optional<Buyer> foundBuyer = buyerRepository.findById(buyerId);
        if(foundBuyer.isEmpty()) throw new NotFoundException("Buyer");
        return foundBuyer.get();
    }

    /**
     * Metodo que verifica se produto existe e o retorna.
     * @param productId identificador do produto.
     * @return Objeto Product contendo infos do produto.
     */
    private Product findProductById(long productId) {
        Optional<Product> foundProduct = productRepository.findById(productId);
        if(foundProduct.isEmpty()) throw new NotFoundException("Product");
        return foundProduct.get();
    }

    /**
     * Metodo que verifica se algum dos batches de um produto está com data valida e tem estoque.
     * @param batches lista de Batch relacionados a um produto
     * @param product ProductDto com informação de quantidades desejadas
     * @param orderStatus status da compra (ABERTA ou FECHADA).
     * @return objeto Batch que seja válido.
     */
    private Batch checkQuantityAndDueDate(List<Batch> batches, ProductDto product, String orderStatus) {
        Batch batchProduct = batches.stream()
                .filter((batch) -> batch.getDueDate().minus(21, ChronoUnit.DAYS).compareTo(LocalDate.now()) > 0)
                .filter((batch) -> batch.getCurrentQuantity() >= product.getQuantity())
                .findFirst()
                .orElseThrow(() -> new ProductOutOfStockException("Product with id "+product.getProductId()));

        if (orderStatus.equals("Closed")) {
            int result = batchProduct.getCurrentQuantity() - product.getQuantity();
            batchProduct.setCurrentQuantity(result);
        }
        return batchProduct;
    }

    /**
     * Metodo que calcula o preço total (quantidade comprada * preço do item no estoque)
     * @param batches lista de Batch para procurar o Batch de um produto.
     * @param products Lista de ProductDto contendo a lista de produtos.
     * @return valor BigDecimal.
     */
    private BigDecimal sumTotalPrice(List<Batch> batches, List<ProductDto> products) {
        return products
                .stream()
                .map(product -> batches
                        .stream().filter(b -> b.getProduct().getProductId() == product.getProductId())
                        .map(batch -> batch.getProductPrice().multiply(new BigDecimal(product.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}