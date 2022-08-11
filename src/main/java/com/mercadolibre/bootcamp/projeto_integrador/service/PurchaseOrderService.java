package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.ProductOutOfStockException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.PurchaseOrderAlreadyClosedException;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     *  Metodo que cria um carrinho (PurchaseOrder) novo ou insere/atualiza itens em um carrinho existente.
     * @param request objeto PurchaseOrderRequestDto.
     * @return valor BigDecimal do valor total em carrinho.
     */
    @Transactional
    @Override
    public BigDecimal create(PurchaseOrderRequestDto request) {
        Buyer buyer = findBuyer(request.getBuyerId());
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, "Opened");
        List<ProductDto> products = request.getProducts();
        if(purchaseOrder == null) {
            purchaseOrder = new PurchaseOrder(request);
            purchaseOrder.setBuyer(buyer);
            purchaseOrder.setDate(LocalDate.now());
            purchaseOrderRepository.save(purchaseOrder);
        } else {
            List<ProductDto> productList = findProductsDtoByPurchaseOrder(purchaseOrder);
            purchaseOrder.getBatchPurchaseOrders().stream()
                    .forEach(batchPurchaseOrder -> batchPurchaseOrder.getBatch()
                            .setCurrentQuantity(batchPurchaseOrder.getBatch().getCurrentQuantity()+batchPurchaseOrder.getQuantity()));
            products = Stream.concat(productList.stream(), products.stream())
                    .collect(Collectors.groupingBy(
                            a -> a.getProductId(),
                            Collectors.summingInt(ProductDto::getQuantity)
                    )).entrySet().stream().map(a -> new ProductDto(a.getKey(), a.getValue())).collect(Collectors.toList());
        }

        List<Batch> foundBatches = findBatches(products);
        return sumTotalPrice(foundBatches, products, purchaseOrder);
    }

    /**
     * Metodo que atualiza o carrinho (PurchaseOrder) para fechado e efetivamente remove do estoque.
     * @param purchaseOrderId identificador do carrinho.
     * @return valor BigDecimal do valor total da compra.
     */
    @Transactional
    @Override
    public BigDecimal update(long purchaseOrderId) {
        PurchaseOrder foundOrder = findOrder(purchaseOrderId);

        if (foundOrder.getOrderStatus().equals("Closed")) {
            throw new PurchaseOrderAlreadyClosedException(foundOrder.getPurchaseId());
        }

        foundOrder.setOrderStatus("Closed");
        List<ProductDto> productsList = findProductsDtoByPurchaseOrder(foundOrder);
        List<Batch> foundBatches = findBatches(productsList);
        purchaseOrderRepository.save(foundOrder);

        return sumTotalPrice(foundBatches, productsList, foundOrder);
    }

    /**
     * Metodo para retornar os DTO dos produtos de uma PurchaseOrder.
     * @param purchaseOrder objeto PurchaseOrder.
     * @return Lista de ProductDto
     */
    private List<ProductDto> findProductsDtoByPurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getBatchPurchaseOrders().stream()
                .map(BatchPurchaseOrder::getBatch)
                .map(batch -> new ProductDto(batch.getProduct().getProductId(), purchaseOrder.getBatchPurchaseOrders()
                        .stream().filter(batchPurchaseOrder -> batchPurchaseOrder.getBatch().equals(batch)).findFirst().get().getQuantity()))
                .collect(Collectors.toList());
    }

    /**
     * Metodo que procura um batch por produto passado.
     * @param products lista de ProductDto contendo os ids dos produtos para procurar os batches.
     * @return Lista de Batch, um para cada produto.
     */
    private List<Batch> findBatches(List<ProductDto> products) {
        return products.stream()
                .map((product) -> {
                    Product p = findProductById(product.getProductId());
                    return checkQuantityAndDueDate(batchRepository.findByProduct(p), product);
                })
                .collect(Collectors.toList());
    }

    /**
     * Metodo que procura por uma PurchaseOrder já existente.
     * @param purchaseOrderId identificador da PurchaseOrder.
     * @return objeto PurchaseOrder encontrado.
     */
    private PurchaseOrder findOrder(long purchaseOrderId) {
        Optional<PurchaseOrder> foundOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if (foundOrder.isEmpty()) throw new NotFoundException("Purchase order");
        return foundOrder.get();
    }

    /**
     * Metodo que verifica se comprador existe e o retorna.
     * @param buyerId identificador do comprador.
     * @return Objeto Buyer contendo infos do comprador.
     */
    private Buyer findBuyer(long buyerId) {
        Optional<Buyer> foundBuyer = buyerRepository.findById(buyerId);
        if (foundBuyer.isEmpty()) throw new NotFoundException("Buyer");
        return foundBuyer.get();
    }

    /**
     * Metodo que verifica se produto existe e o retorna.
     * @param productId identificador do produto.
     * @return Objeto Product contendo infos do produto.
     */
    private Product findProductById(long productId) {
        Optional<Product> foundProduct = productRepository.findById(productId);
        if (foundProduct.isEmpty()) throw new NotFoundException("Product");
        return foundProduct.get();
    }

    /**
     * Metodo que verifica se algum dos batches de um produto está com data valida e tem estoque.
     * @param batches     lista de Batch relacionados a um produto
     * @param product     ProductDto com informação de quantidades desejadas
     * @return objeto Batch que seja válido.
     */
    private Batch checkQuantityAndDueDate(List<Batch> batches, ProductDto product) {
        Batch batchProduct = batches.stream()
                .filter((batch) -> batch.getDueDate().minus(21, ChronoUnit.DAYS).compareTo(LocalDate.now()) > 0)
                .filter((batch) -> batch.getCurrentQuantity() >= product.getQuantity())
                .findFirst()
                .orElseThrow(() -> new ProductOutOfStockException(product.getProductId()));

        int result = batchProduct.getCurrentQuantity() - product.getQuantity();
        batchProduct.setCurrentQuantity(result);
        return batchProduct;
    }

    /**
     * Metodo que salva a relação nxm de Batch e PurchaseOrder e retorna o preço total (quantidade comprada * preço do item no estoque)
     * @param batches  lista de Batch para procurar o Batch de um produto.
     * @param products Lista de ProductDto contendo a lista de produtos.
     * @return valor BigDecimal.
     */
    private BigDecimal sumTotalPrice(List<Batch> batches, List<ProductDto> products, PurchaseOrder purchase) {
        return products
                .stream()
                .map(product -> saveBatchPurchaseOrder(batches, product, purchase))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Metodo que retorna o objeto intermediário da relação nxm entre Batch e PurchaseOrder.
     * @param purchase objeto PurchaseOrder.
     * @param batch objeto Batch
     * @return Objeto BatchPurchaseOrder.
     */
    private BatchPurchaseOrder findBatchPurchaseOrder(PurchaseOrder purchase, Batch batch) {
        Optional<BatchPurchaseOrder> foundBatchPurchaseOrder = batchPurchaseOrderRepository.findOneByBatchAndPurchaseOrder(batch, purchase);
        if (foundBatchPurchaseOrder.isEmpty()) throw new NotFoundException("Batch PurchaseOrder");
        return foundBatchPurchaseOrder.get();
    }

    /**
     * @param product ProductDto contendo a quantidade desejada.
     * @param purchase objeto Purchase que será usado na relação nxm.
     * @return valor BigDecimal.
     */
    private BigDecimal saveBatchPurchaseOrder(List<Batch> batches, ProductDto product, PurchaseOrder purchase){
        Batch batch = batches.stream()
                .filter(b -> b.getProduct().getProductId() == product.getProductId())
                .findFirst().get();

        BatchPurchaseOrder batchPurchaseOrder;

        try {
            batchPurchaseOrder = findBatchPurchaseOrder(purchase, batch);
        } catch(NotFoundException ex){
            batchPurchaseOrder = new BatchPurchaseOrder();
            batchPurchaseOrder.setPurchaseOrder(purchase);
            batchPurchaseOrder.setBatch(batch);
            batchPurchaseOrder.setUnitPrice(batch.getProductPrice());
        }
        batchPurchaseOrder.setQuantity(product.getQuantity());
        batchPurchaseOrderRepository.save(batchPurchaseOrder);

        return batch.getProductPrice().multiply(new BigDecimal(product.getQuantity()));
    }
}