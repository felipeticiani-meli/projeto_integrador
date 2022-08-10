package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchDueDateResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.*;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.InboundOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BatchService implements IBatchService {

    private final int minimumExpirationDays = 20;
    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IManagerRepository managerRepository;
    @Autowired
    ISectionRepository sectionRepository;

    @Override
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
     * Método que busca a lista de Batches com estoque positivo e data de validade superior a 20 dias, filtrado por categoria.
     *
     * @param categoryCode
     * @return List<Batch>
     */
    @Override
    public List<BatchBuyerResponseDto> findBatchByCategory(String categoryCode) {
        Section.Category category = getCategory(categoryCode);
        LocalDate minimumExpirationDate = LocalDate.now().plusDays(minimumExpirationDays);
        List<Batch> batches = batchRepository
                .findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(0, minimumExpirationDate, category);
        if (batches.isEmpty()) {
            throw new NotFoundException("Products", "There are no products in stock in the requested category");
        }
        return mapListBatchToListDto(batches);
    }

    /**
     * Método que retorna os lotes filtrados por seção em ordem crescente da data de validade
     *
     * @param sectionCode Código da seção
     * @param managerId
     * @return Lista de lotes
     */
    @Override
    public List<BatchDueDateResponseDto> findBatchBySection(long sectionCode, long managerId) {
        Section section = sectionRepository.findById(sectionCode).orElseThrow(() -> new NotFoundException("section"));

        Manager manager = tryFindManagerById(managerId);
        ensureManagerHasPermissionInSection(manager, section);

        return batchRepository.findByInboundOrder_SectionOrderByDueDate(section)
                .stream()
                .map(BatchDueDateResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Método que retorna os lotes filtrados por categoria e data de vencimento e ordenaodos por categoria
     *
     * @param categoryCode Código da categoria
     * @param numberOfDays Número de dias mínimo até expirar os produtos
     * @param managerId
     * @return Lista de lotes
     */
    @Override
    public List<BatchDueDateResponseDto> findBatchByCategoryAndDueDate(String categoryCode,
                                                                       int numberOfDays,
                                                                       long managerId) {
        tryFindManagerById(managerId);

        Section.Category category = getCategory(categoryCode);
        LocalDate minimumExpirationDate = LocalDate.now().plusDays(numberOfDays);

        return batchRepository.findByProduct_CategoryAndDueDateAfterOrderByProduct_Category(category, minimumExpirationDate)
                .stream()
                .filter(batch -> batch.getInboundOrder().getSection().getManager().getManagerId() == managerId)
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

    private void ensureManagerHasPermissionInSection(Manager manager, Section section) {
        if (section.getManager().getManagerId() != manager.getManagerId())
            throw new UnauthorizedManagerException(manager.getName());
    }

    private Manager tryFindManagerById(long managerId) {
        return managerRepository.findById(managerId).orElseThrow(() -> new ManagerNotFoundException(managerId));
    }
}
