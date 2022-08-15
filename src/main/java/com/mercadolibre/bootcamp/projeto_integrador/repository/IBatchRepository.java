package com.mercadolibre.bootcamp.projeto_integrador.repository;

import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findAllByProduct(Product product);
    List<Batch> findByCurrentQuantityGreaterThanAndDueDateAfter(int minimumQuantity, LocalDate minimumExpirationDate);
    List<Batch> findByCurrentQuantityGreaterThanAndDueDateAfterAndProduct_CategoryIs(
            int minimumQuantity, LocalDate minimumExpirationDate, Section.Category category);

    List<Batch> findByInboundOrder_SectionAndDueDateBetweenOrderByDueDate(
            Section section, LocalDate startDate, LocalDate endDate);

    List<Batch> findByProduct_CategoryAndDueDateBetweenOrderByDueDateAsc(
            Section.Category category, LocalDate startDate, LocalDate endDate);

    List<Batch> findByProduct_CategoryAndDueDateBetweenOrderByDueDateDesc(
            Section.Category category, LocalDate startDate, LocalDate endDate);

    Optional<Batch> findOneByBatchNumberAndCurrentQuantityGreaterThanEqualAndDueDateAfterOrderByDueDate(long batchNumber, int minimumQuantity, LocalDate minimumExpirationDate);
}
