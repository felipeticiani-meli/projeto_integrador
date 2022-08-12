package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchDueDateResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.*;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.Converter;
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
    private IManagerRepository managerRepository;
    @Autowired
    private ISectionRepository sectionRepository;

    @Autowired
    private IProductService productService;

    /**
     * Metodo que faz o map do DTO de Batch para um objeto Batch e já lhe atribui um produto (que deve existir).
     *
     * @param dto      objeto BatchRequestDto que é recebido na requisição.
     * @param order    ordem de entrada
     * @param products mapa de Product.
     * @return Objeto Batch montado com um produto atribuido.
     */
    public static Batch mapDtoToBatch(BatchRequestDto dto, InboundOrder order, Map<Long, Product> products) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            Converter<Long, Product> converter = context -> products.get(context.getSource());
            mapper.using(converter).map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(dto, Batch.class);
        if (batch.getProduct() == null)
            throw new NotFoundException("Product");
        batch.setInboundOrder(order);
        return batch;
    }

    @Override
    public List<Batch> createAll(List<BatchRequestDto> batchesDto, InboundOrder order) {
        Map<Long, Product> products = productService.getProductMap(batchesDto);
        List<Batch> batches = buildBatchesForCreate(batchesDto, order, products);
        return batchRepository.saveAll(batches);
    }

    @Override
    public List<Batch> updateAll(InboundOrder order, List<BatchRequestDto> batchesDto) {
        Map<Long, Product> products = productService.getProductMap(batchesDto);
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

        Stream<Batch> updatedBatches = batchesToUpdate.stream()
                .map(batch -> updateBatchFromDto(batch, batchesDtoMap.get(batch.getBatchNumber()), products));

        Stream<Batch> batchesToInsert = batchesDto.stream()
                .filter(dto -> dto.getBatchNumber() == 0L)
                .map(dto -> mapDtoToBatch(dto, order, products))
                .peek(batch -> batch.setCurrentQuantity(batch.getInitialQuantity()));

        List<Batch> batchesToSave = Stream.concat(updatedBatches, batchesToInsert).collect(Collectors.toList());

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
    private List<Batch> buildBatchesForCreate(List<BatchRequestDto> batchesDto, InboundOrder order, Map<Long, Product> products){
        return batchesDto.stream()
                .map(dto -> mapDtoToBatch(dto, order, products))
                .peek(batch -> batch.setBatchNumber(0L))
                .peek(batch -> batch.setCurrentQuantity(batch.getInitialQuantity()))
                .collect(Collectors.toList());
    }

    private Batch updateBatchFromDto(Batch batch, BatchRequestDto dto, Map<Long, Product> products) {
        Product product = products.get(dto.getProductId());

        if (product == null) {
            throw new NotFoundException("Product");
        }

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
     * Método que retorna os lotes filtrados por seção em ordem crescente da data de validade
     *
     * @param sectionCode  Código da seção
     * @param numberOfDays Número de dias a partir de hoje
     * @param managerId    ID do representante
     * @return Lista de lotes
     */
    @Override
    public List<BatchDueDateResponseDto> findBatchBySection(long sectionCode, int numberOfDays, long managerId) {
        if (numberOfDays < 0)
            throw new BadRequestException("The number of days to expiration can't be negative");

        Section section = sectionRepository.findById(sectionCode).orElseThrow(() -> new NotFoundException("section"));

        Manager manager = tryFindManagerById(managerId);
        ensureManagerHasPermissionInSection(manager, section);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(numberOfDays);

        return batchRepository.findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(section, startDate, endDate)
                .stream()
                .filter(batch -> batch.getCurrentQuantity() > 0)
                .map(BatchDueDateResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Método que retorna os lotes filtrados por categoria e data de vencimento e ordenados por data de vencimento
     *
     * @param categoryCode Código da categoria
     * @param numberOfDays Número de dias mínimo até expirar os produtos
     * @param orderDir     Direção da ordenação
     * @param managerId    ID do representante
     * @return Lista de lotes
     */
    @Override
    public List<BatchDueDateResponseDto> findBatchByCategoryAndDueDate(String categoryCode,
                                                                       int numberOfDays,
                                                                       String orderDir,
                                                                       long managerId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(numberOfDays);
        Section.Category category = getCategory(categoryCode);
        String orderDirection = StringUtils.trimToEmpty(orderDir);

        tryFindManagerById(managerId);

        if (numberOfDays < 0)
            throw new BadRequestException("The number of days to expiration can't be negative");

        if (!StringUtils.equalsAnyIgnoreCase(orderDirection, "ASC", "DESC"))
            throw new BadRequestException("The order direction should be either ASC or DESC");

        List<Batch> batches = orderDirection.equalsIgnoreCase("ASC")
                ? batchRepository.findByProduct_CategoryAndDueDateBetweenOrderByDueDateAsc(category, startDate, endDate)
                : batchRepository.findByProduct_CategoryAndDueDateBetweenOrderByDueDateDesc(category, startDate, endDate);

        return batches.stream()
                .filter(batch -> batch.getInboundOrder().getSection().getManager().getManagerId() == managerId)
                .filter(batch -> batch.getCurrentQuantity() > 0)
                .map(BatchDueDateResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Método converte a lista de Batch para uma lista de BatchBuyerResponseDto.
     *
     * @param batches
     * @return List<BatchBuyerResponseDto>
     */
    private List<BatchBuyerResponseDto> mapListBatchToListDto(List<Batch> batches) {
        return batches.stream()
                .map(BatchBuyerResponseDto::new)
                .collect(Collectors.toList());
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

    private void ensureManagerHasPermissionInSection(Manager manager, Section section) {
        if (section.getManager().getManagerId() != manager.getManagerId())
            throw new UnauthorizedManagerException(manager.getName());
    }

    private Manager tryFindManagerById(long managerId) {
        return managerRepository.findById(managerId).orElseThrow(() -> new ManagerNotFoundException(managerId));
    }
}
