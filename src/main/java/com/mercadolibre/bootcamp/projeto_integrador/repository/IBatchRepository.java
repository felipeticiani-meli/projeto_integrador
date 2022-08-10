package com.mercadolibre.bootcamp.projeto_integrador.repository;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IBatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByCurrentQuantityGreaterThanAndDueDateAfter(int minimumQuantity, LocalDate minimumExpirationDate);

    List<Batch> findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(
            int minimumQuantity, LocalDate minimumExpirationDate, Section.Category category);

    List<Batch> findByInboundOrder_SectionOrderByDueDate(Section section);

    List<Batch> findByProduct_CategoryAndDueDateAfterOrderByProduct_Category(Section.Category category, LocalDate minimumExpirationDate);
}
