package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.ManagerNotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.MaxSizeException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.UnauthorizedManagerException;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        Optional<Manager> manager = managerRepository.findById(managerId);
        if (manager.isEmpty())
            throw new ManagerNotFoundException(managerId);

        if (section.getManager().getManagerId() != managerId)
            throw new UnauthorizedManagerException(manager.get().getName());

        sectionHasSpace(section, request.getBatchStock().size());

        List<Batch> batches = buildBatches(request.getBatchStock());

        InboundOrder order = new InboundOrder();
        order.setSection(section);
        order.setOrderDate(LocalDate.now());

        sectionRepository.save(section);
        inboundOrderRepository.save(order);
        batchRepository.saveAll(batches.stream().peek(batch -> batch.setInboundOrder(order)).collect(Collectors.toList()));

        return new InboundOrderResponseDto() {{ setBatchStock(batches); }};
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

        List<Batch> batches = buildBatches(request.getBatchStock());

        batches.forEach(b -> b = batchService.update(order, b));

        sectionRepository.save(sectionHasSpace(order.getSection(), (int) request.getBatchStock().stream()
                .filter(b -> b.getBatchNumber() == 0)
                .count()));

        return new InboundOrderResponseDto() {{ setBatchStock(batches); }};
    }

    /**
     * Metodo que monta uma lista de Batch, dada lista de DTO da requisição.
     * @param batchesDto lista de BatchRequestDto.
     * @return List<Batch> pronto.
     */
    private List<Batch> buildBatches(List<BatchRequestDto> batchesDto){
        Map<Long, Product> products = productRepository
                .findAllById(batchesDto.stream().map(BatchRequestDto::getProductId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        return batchesDto.stream()
                .map(dto -> mapDtoToBatch(dto, products))
                .collect(Collectors.toList());

    }

    /**
     * Metodo que verifica se uma seção tem slots disponiveis para um ou mais novos lotes, e já atualiza o número de slots utilizados.
     * @param section objeto Section.
     * @param batchCount quantos novos lotes estão sendo alocados.
     * @return Objeto Section com o número de slots utilizados atualizado.
     */
    private Section sectionHasSpace(Section section, int batchCount){
        if (section.getAvailableSlots() < batchCount) {
            throw new MaxSizeException("Section");
        }
        section.setCurrentBatches(section.getCurrentBatches() + batchCount);
        return section;
    }

    /**
     * Metodo que faz o map do DTO de Batch para um objeto Batch e já lhe atribui um produto (que deve existir).
     * @param dto objeto BatchRequestDto que é recebido na requisição.
     * @param products lista de Product.
     * @return Objeto Batch montado com um produto atribuido.
     */
    private Batch mapDtoToBatch(BatchRequestDto dto, Map<Long, Product> products) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            Converter<Long, Product> converter = context -> products.get(context.getSource());
            mapper.using(converter).map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(dto, Batch.class);
        if (batch.getProduct() == null)
            throw new NotFoundException("Product");

        batch.setCurrentQuantity(dto.getInitialQuantity());
        return batch;
    }
}
