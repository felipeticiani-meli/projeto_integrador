package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.IncompatibleCategoryException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.MaxSizeException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.NotFoundException;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.UnauthorizedManagerException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SectionService implements ISectionService {
    @Autowired
    private ISectionRepository sectionRepository;

    @Autowired
    private IManagerService managerService;

    @Autowired
    private IProductService productService;

    @Override
    public Section findById(long sectionCode) {
        return sectionRepository.findById(sectionCode).orElseThrow(() -> new NotFoundException("Section"));
    }

    @Override
    public Section update(Section section, List<BatchRequestDto> batchesToInsert, long managerId) {
        Map<Long, Product> products = productService.getProductMap(batchesToInsert);

        ensureManagerHasPermissionInSection(managerId, section);
        ensureSectionHasCompatibleCategory(section, products);
        ensureSectionHasSpace(section, batchesToInsert.size());

        sectionRepository.save(section);

        return section;
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
        Manager manager = managerService.findById(managerId);

        if (section.getManager().getManagerId() != managerId)
            throw new UnauthorizedManagerException(manager.getName());
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
