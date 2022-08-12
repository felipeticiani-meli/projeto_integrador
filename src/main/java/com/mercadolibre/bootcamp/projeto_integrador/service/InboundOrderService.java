package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.*;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mercadolibre.bootcamp.projeto_integrador.service.BatchService.mapDtoToBatch;

@Service
public class InboundOrderService implements IInboundOrderService {

    @Autowired
    private IBatchService batchService;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ISectionRepository sectionRepository;

    @Autowired
    private IInboundOrderRepository inboundOrderRepository;

    @Autowired
    private IBatchRepository batchRepository;

    @Autowired
    private IManagerRepository managerRepository;

    /**
     * Método que faz a criação da InboundOrder com novos lotes
     * @param request InboundOrderRequestDto
     * @return InboundOrderResponseDto contendo os dados dos lotes inseridos
     */
    @Override
    @Transactional
    public InboundOrderResponseDto create(InboundOrderRequestDto request, long managerId) {
        Optional<Section> foundSection = sectionRepository.findById(request.getSectionCode());
        if (foundSection.isEmpty())
            throw new NotFoundException("Section");

        Section section = foundSection.get();
        Map<Long, Product> products = getProductMap(request.getBatchStock());

        ensureManagerHasPermissionInSection(managerId, section);
        ensureSectionHasCompatibleCategory(section, products);
        ensureSectionHasSpace(section, request.getBatchStock().size());

        InboundOrder order = new InboundOrder();
        order.setSection(section);
        order.setOrderDate(LocalDate.now());

        sectionRepository.save(section);
        inboundOrderRepository.save(order);

        List<Batch> batches = buildBatchesForCreate(request.getBatchStock(), order, products);

        batchRepository.saveAll(batches);

        return new InboundOrderResponseDto(batches);
    }

    /**
     * Metodo que faz a atualização da InboundOrder, com novos lotes ou atualiza os que já estão dentro
     * @param orderNumber long representando o id da InboundOrder
     * @param request objeto InboundOrderRequestDto
     * @return InboundOrderResponseDto contendo as infos dos lotes atualizados/inseridos
     */
    @Override
    @Transactional
    public InboundOrderResponseDto update(long orderNumber, InboundOrderRequestDto request, long managerId) {
        InboundOrder order = inboundOrderRepository.findById(orderNumber)
                .orElseThrow(() -> new NotFoundException("Inbound Order"));

        Section section = order.getSection();
        List<BatchRequestDto> batchesDto = request.getBatchStock();
        Map<Long, Product> products = getProductMap(batchesDto);

        ensureManagerHasPermissionInSection(managerId, section);
        ensureSectionHasCompatibleCategory(section, products);
        ensureSectionHasSpace(section, (int) batchesDto.stream().filter(b -> b.getBatchNumber() == 0L).count());

        List<Batch> batchesToSave = batchService.updateAll(order, batchesDto, products);

        batchRepository.saveAll(batchesToSave);
        sectionRepository.save(section);

        return new InboundOrderResponseDto(batchesToSave);
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

    /**
     * Retorna mapa de produtos por ID
     * @param batchesDto Lotes enviados no pedido de entrada
     * @return Mapa de produtos com identificador como chave
     */
    private Map<Long, Product> getProductMap(List<BatchRequestDto> batchesDto) {
        return productRepository
                .findAllById(batchesDto.stream().map(BatchRequestDto::getProductId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));
    }

    /**
     * Garante que a seção pode acomodar todos os produtos fornecidos.
     * @param section Seção dos lotes
     * @param products Produtos
     */
    private void ensureSectionHasCompatibleCategory(Section section, Map<Long, Product> products) {
        List<Product> invalidProducts = products.values()
                .stream()
                .filter(product -> !product.getCategory().equals(section.getCategory()))
                .collect(Collectors.toList());

        List<String> productNames = invalidProducts.stream().map(Product::getProductName).collect(Collectors.toList());

        if (invalidProducts.size() > 0)
            throw new IncompatibleCategoryException(productNames);
    }

    /**
     * Garante que o gerente tem permissão para lidar na seção fornecida.
     * @param managerId ID do gerente
     * @param section Seção dos lotes
     */
    private void ensureManagerHasPermissionInSection(long managerId, Section section) {
        Optional<Manager> manager = managerRepository.findById(managerId);

        if (manager.isEmpty())
            throw new ManagerNotFoundException(managerId);

        if (section.getManager().getManagerId() != managerId)
            throw new UnauthorizedManagerException(manager.get().getName());
    }

    /**
     * Metodo que verifica se uma seção tem slots disponiveis para um ou mais novos lotes, e já atualiza o número de
     * slots utilizados.
     *
     * @param section    objeto Section.
     * @param batchCount quantos novos lotes estão sendo alocados.
     */
    private void ensureSectionHasSpace(Section section, int batchCount){
        if (section.getAvailableSlots() < batchCount) {
            throw new MaxSizeException("Section");
        }
        section.setCurrentBatches(section.getCurrentBatches() + batchCount);
    }
}
