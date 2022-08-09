package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.exceptions.BadRequestException;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    /**
     * Ordena uma lista de Batch de acordo com o parâmetro recebido.
     * @param batches
     * @param order L: batchNumber, Q: currentQuantity, V: dueDate.
     * @return Lista ordenada de batches.
     * @throws BadRequestException Caso o parâmetro seja inválido.
     */
    private List<Batch> order(List<Batch> batches, String order) throws BadRequestException {
        switch (order.toUpperCase()) {
            case "L": return batches.stream()
                    .sorted(Comparator.comparing(Batch::getBatchNumber))
                    .collect(Collectors.toList());
            case "Q": return batches.stream()
                    .sorted(Comparator.comparing(Batch::getCurrentQuantity))
                    .collect(Collectors.toList());
            case "V": return batches.stream()
                    .sorted(Comparator.comparing(Batch::getDueDate))
                    .collect(Collectors.toList());
            default: throw new BadRequestException("Parâmetro de ordenação inválido. L: batchNumber, Q: currentQuantity, V: dueDate");
        }
    }
}
