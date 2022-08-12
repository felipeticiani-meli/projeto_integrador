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
import java.util.ArrayList;
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
            // Produtos que já estavam no carrinho.
            List<ProductDto> productList = findProductsDtoByPurchaseOrder(purchaseOrder);
            // É retornada a quantidade extraida (pela compra) do estoque de cada batch,
            // pois como as quantidades são agregadas, não deve-se retirar novamente o que já foi retirado.
            purchaseOrder.getBatchPurchaseOrders().stream()
                    .forEach(batchPurchaseOrder -> batchPurchaseOrder.getBatch()
                            .setCurrentQuantity(batchPurchaseOrder.getBatch().getCurrentQuantity()+batchPurchaseOrder.getQuantity()));
            // Agrupamento pelo id do produto, somando-se as quantidades.
            products = Stream.concat(productList.stream(), products.stream())
                    .collect(Collectors.groupingBy(
                            a -> a.getProductId(),
                            Collectors.summingInt(ProductDto::getQuantity)
                    )).entrySet().stream().map(a -> new ProductDto(a.getKey(), a.getValue())).collect(Collectors.toList());
        }

        return getPurchaseInStock(products, purchaseOrder);
    }

    /**
     * Metodo que atualiza o carrinho (PurchaseOrder) para fechado.
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
        purchaseOrderRepository.save(foundOrder);

        return foundOrder.getBatchPurchaseOrders().stream()
                .map(batchPurchaseOrder -> batchPurchaseOrder.getUnitPrice().multiply(new BigDecimal(batchPurchaseOrder.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Metodo que remove produto do carrinho.
     * @param purchaseOrderId identificador do carrinho (PurchaseOrder).
     * @param productsDto identificadores dos produtos.
     */
    @Override
    public void dropProducts(long purchaseOrderId, List<ProductDto> productsDto) {
        for(ProductDto productDto: productsDto){
            batchPurchaseOrderRepository.delete(returnToStock(findBatchPurchaseOrder(findOrder(purchaseOrderId), findProductById(productDto.getProductId()))));
        }
    }

    /**
     * Metodo que devolve ao estoque a quantidade que estava no carrinho.
     * @param batchPurchaseOrder objeto da tabela nxm BatchPurchaseOrder.
     * @return o próprio objeto BatchPurchaseOrder.
     */
    private BatchPurchaseOrder returnToStock(BatchPurchaseOrder batchPurchaseOrder) {
        batchPurchaseOrder.getBatch().setCurrentQuantity(batchPurchaseOrder.getBatch().getCurrentQuantity()+batchPurchaseOrder.getQuantity());
        return batchPurchaseOrder;
    }

    /**
     * Metodo que procura um batch por produto passado.
     * @param products lista de ProductDto contendo os ids dos produtos para procurar os batches.
     * @param purchase objeto PurchaseOrder sendo a compra atual para vincular os produtos.
     * @return Lista de Batch, um para cada produto.
     */
    private BigDecimal getPurchaseInStock(List<ProductDto> products, PurchaseOrder purchase) {
        List<Long> productsIds = new ArrayList<>();
        // Procura por algum batch disponivel para cada produto.
        List<Batch> batches = products.stream()
                .map((product) -> {
                    Product p = findProductById(product.getProductId());
                    try {
                        return checkQuantityAndDueDate(batchRepository.findByProduct(p), product);
                    }catch (ProductOutOfStockException ex){
                        // Se não encontrar nenhum batch para o produto, produto será adicionado ao erro retornado para o usuário.
                        productsIds.add(product.getProductId());
                    }
                    return null;
                })
                .filter(batch -> batch != null)
                .collect(Collectors.toList());

        BigDecimal price = sumTotalPrice(batches, products, purchase);
        if(!productsIds.isEmpty()){
            throw new ProductOutOfStockException(productsIds);
        }
        return price;
    }

    /**
     * Metodo que verifica se algum dos batches de um produto está com data valida e tem estoque.
     * @param batches     lista de Batch relacionados a um produto
     * @param product     ProductDto com informação de quantidades desejadas
     * @return objeto Batch que seja válido.
     */
    @Transactional
    Batch checkQuantityAndDueDate(List<Batch> batches, ProductDto product) {
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
        return batches
                .stream()
                .map(batch -> saveBatchPurchaseOrder(batch, products, purchase))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Metodo cria uma nova tabela nxm ou atualiza a já existente.
     * @param batch objeto Batch disponivel para descontar quantidade do produto.
     * @param products lista de ProductDto.
     * @param purchase objeto Purchase que será usado na relação nxm.
     * @return valor BigDecimal do valor total de cada item comprado.
     */
    private BigDecimal saveBatchPurchaseOrder(Batch batch, List<ProductDto> products, PurchaseOrder purchase){
        ProductDto productDto = products.stream()
                .filter(p -> p.getProductId() == batch.getProduct().getProductId())
                .findFirst().get();

        BatchPurchaseOrder batchPurchaseOrder;

        // Se já existir a tabela nxm entre um batch e uma purchase ela só é atualizada com a nova quantidade.
        try {
            batchPurchaseOrder = findBatchPurchaseOrder(purchase, batch);
        } catch(NotFoundException ex){
            batchPurchaseOrder = new BatchPurchaseOrder();
            batchPurchaseOrder.setPurchaseOrder(purchase);
            batchPurchaseOrder.setBatch(batch);
            batchPurchaseOrder.setUnitPrice(batch.getProductPrice());
        }
        batchPurchaseOrder.setQuantity(productDto.getQuantity());
        batchPurchaseOrderRepository.save(batchPurchaseOrder);

        return batch.getProductPrice().multiply(new BigDecimal(productDto.getQuantity()));
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
     * Metodo que retorna o objeto intermediário da relação nxm entre Batch e PurchaseOrder.
     * @param purchase objeto PurchaseOrder.
     * @param batch objeto Batch
     * @return Objeto BatchPurchaseOrder.
     */
    private BatchPurchaseOrder findBatchPurchaseOrder(PurchaseOrder purchase, Batch batch) {
        Optional<BatchPurchaseOrder> foundBatchPurchaseOrder = batchPurchaseOrderRepository.findOneByPurchaseOrderAndBatch(purchase, batch);
        if (foundBatchPurchaseOrder.isEmpty()) throw new NotFoundException("Batch PurchaseOrder");
        return foundBatchPurchaseOrder.get();
    }

    private BatchPurchaseOrder findBatchPurchaseOrder(PurchaseOrder purchase, Product product) {
        Optional<BatchPurchaseOrder> foundBatchPurchaseOrder = batchPurchaseOrderRepository.findOneByPurchaseOrderAndBatch_Product(purchase, product);
        if (foundBatchPurchaseOrder.isEmpty()) throw new NotFoundException("Product in the PurchaseOrder");
        return foundBatchPurchaseOrder.get();
    }
}