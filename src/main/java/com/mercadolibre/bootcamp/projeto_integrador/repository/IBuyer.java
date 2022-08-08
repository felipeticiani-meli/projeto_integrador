package com.mercadolibre.bootcamp.projeto_integrador.repository;

import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBuyer extends JpaRepository<Buyer, Long> {
}
