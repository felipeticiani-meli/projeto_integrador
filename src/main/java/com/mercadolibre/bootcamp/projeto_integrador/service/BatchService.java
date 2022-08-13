package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.InitialQuantityException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BatchService implements IBatchService {

    private final int minimumExpirationDays = 20;

    @Autowired
    private IBatchRepository batchRepository;

    @Autowired
    private IProductMapService productService;

    /**
     * Metodo que faz o map do DTO de Batch para um objeto Batch e já lhe atribui um produto (que deve existir).
     *
     * @param dto      objeto BatchRequestDto que é recebido na requisição.
     * @param order    ordem de entrada
     * @param products mapa de Product.
     * @return Objeto Batch montado com um produto atribuido.
     */
    private static Batch mapDtoToBatch(BatchRequestDto dto, InboundOrder order, ProductMapService.ProductMap products) {
        ModelMapper modelMapper = new ModelMapper();
        Batch batch = modelMapper.map(dto, Batch.class);
        batch.setProduct(products.get(dto.getProductId()));
        batch.setInboundOrder(order);
        return batch;
    }

    @Override
    public List<Batch> createAll(List<BatchRequestDto> batchesDto, InboundOrder order) {
        ProductMapService.ProductMap products = productService.getProductMap(batchesDto);
        List<Batch> batches = buildBatchesForCreate(batchesDto, order, products);
        return batchRepository.saveAll(batches);
    }

    @Override
    public List<Batch> updateAll(InboundOrder order, List<BatchRequestDto> batchesDto) {
        ProductMapService.ProductMap products = productService.getProductMap(batchesDto);
        List<Long> batchNumbersToUpdate = batchesDto.stream()
                .map(BatchRequestDto::getBatchNumber)
                .filter(batchNumber -> batchNumber > 0L)
                .collect(Collectors.toList());

        List<Batch> batchesToUpdate = batchRepository.findAllById(batchNumbersToUpdate);

        boolean isAllFromSameOrder = batchesToUpdate
                .stream()
                .allMatch(batch -> batch.getInboundOrder().getOrderNumber() == order.getOrderNumber());

        if (!isAllFromSameOrder)
            throw new BadRequestException("Unable to update batches of different orders");

        Map<Long, BatchRequestDto> batchesDtoMap = batchesDto.stream()
                .filter(dto -> dto.getBatchNumber() > 0L)
                .collect(Collectors.toMap(BatchRequestDto::getBatchNumber, dto -> dto));

        var updatedBatches = batchesToUpdate.stream()
                .map(batch -> updateBatchFromDto(batch, batchesDtoMap.get(batch.getBatchNumber()), products))
                .collect(Collectors.toList());

        var batchesToInsert = batchesDto.stream()
                .filter(dto -> dto.getBatchNumber() == 0L)
                .map(dto -> mapDtoToBatch(dto, order, products))
                .peek(batch -> batch.setCurrentQuantity(batch.getInitialQuantity()))
                .collect(Collectors.toList());

        List<Batch> batchesToSave = Stream.concat(updatedBatches.stream(), batchesToInsert.stream()).collect(Collectors.toList());

        return batchRepository.saveAll(batchesToSave);
    }

    @Override
    @Deprecated
    public Batch update(InboundOrder order, Batch batch) {
        Optional<Batch> b = batchRepository.findById(batch.getBatchNumber());
        batch.setInboundOrder(order);
        if (b.isEmpty()) {
            batch.setCurrentQuantity(batch.getInitialQuantity());
            batchRepository.save(batch);
            return batch;
        }
        int selledProducts = b.get().getInitialQuantity() - b.get().getCurrentQuantity();
        batch.setCurrentQuantity(batch.getInitialQuantity() - selledProducts);
        if (batch.getCurrentQuantity() < 0) {
            throw new InitialQuantityException(batch.getInitialQuantity(), selledProducts);
        }
        batchRepository.save(batch);
        return batch;
    }

    /**
     * Método que busca a lista de Batches com estoque positovo e data de validade superior a 20 dias.
     *
     * @return List<Batch>
     */
    @Override
    public List<BatchBuyerResponseDto> findAll() {
        LocalDate minimumExpirationDate = LocalDate.now().plusDays(minimumExpirationDays);
        List<Batch> batches = batchRepository.findByCurrentQuantityGreaterThanAndDueDateAfter(0, minimumExpirationDate);
        if (batches.isEmpty()) {
            throw new NotFoundException("Products", "There are no products in stock");
        }
        return mapListBatchToListDto(batches);
    }

    /**
     * Método que busca a lista de Batches com estoque positivo e data de validade superior a 20 dias, filtrado por
     * categoria.
     *
     * @param categoryCode
     * @return List<Batch>
     */
    @Override
    public List<BatchBuyerResponseDto> findBatchByCategory(String categoryCode) {
        Section.Category category = getCategory(categoryCode);
        LocalDate minimumExpirationDate = LocalDate.now().plusDays(minimumExpirationDays);
        List<Batch> batches = batchRepository
                .findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(0, minimumExpirationDate,
                        category);
        if (batches.isEmpty()) {
            throw new NotFoundException("Products", "There are no products in stock in the requested category");
        }
        return mapListBatchToListDto(batches);
    }

    /**
     * Metodo que monta uma lista de Batch, dada lista de DTO da requisição.
     * @param batchesDto lista de BatchRequestDto.
     * @return List<Batch> pronto.
     */
    private List<Batch> buildBatchesForCreate(List<BatchRequestDto> batchesDto, InboundOrder order, ProductMapService.ProductMap products){
        return batchesDto.stream()
                .map(dto -> mapDtoToBatch(dto, order, products))
                .peek(batch -> batch.setBatchNumber(0L))
                .peek(batch -> batch.setCurrentQuantity(batch.getInitialQuantity()))
                .collect(Collectors.toList());
    }

    private Batch updateBatchFromDto(Batch batch, BatchRequestDto dto, ProductMapService.ProductMap products) {
        Product product = products.get(dto.getProductId());
        batch.setProduct(product);
        batch.setCurrentTemperature(dto.getCurrentTemperature());
        batch.setMinimumTemperature(dto.getMinimumTemperature());
        batch.setManufacturingDate(dto.getManufacturingDate());
        batch.setManufacturingTime(dto.getManufacturingTime());
        batch.setDueDate(dto.getDueDate());
        batch.setProductPrice(dto.getProductPrice());

        int soldProducts = batch.getInitialQuantity() - batch.getCurrentQuantity();
        batch.setCurrentQuantity(dto.getInitialQuantity() - soldProducts);

        if (batch.getCurrentQuantity() < 0) {
            throw new InitialQuantityException(dto.getInitialQuantity(), soldProducts);
        }

        batch.setInitialQuantity(dto.getInitialQuantity());

        return batch;
    }

    /**
     * Método converte a lista de Batch para uma lista de BatchBuyerResponseDto.
     *
     * @param batches
     * @return List<BatchBuyerResponseDto>
     */
    private List<BatchBuyerResponseDto> mapListBatchToListDto(List<Batch> batches) {
        List<BatchBuyerResponseDto> batchBuyerResponse = batches.stream()
                .map(batch -> new BatchBuyerResponseDto(batch))
                .collect(Collectors.toList());
        return batchBuyerResponse;
    }

    /**
     * Método que retorna a categoria do produto dado o código da cateogria.
     *
     * @param categoryCode
     * @return String category
     */
    private Section.Category getCategory(String categoryCode) {
        categoryCode = categoryCode.toUpperCase();
        switch (categoryCode) {
            case "FS":
                return Section.Category.FRESH;
            case "RF":
                return Section.Category.CHILLED;
            case "FF":
                return Section.Category.FROZEN;
            default:
                throw new BadRequestException("Invalid category, try again with one of the options: " +
                        "'FS', 'RF' or 'FF' for fresh, chilled or frozen products respectively.");
        }
    }
}
