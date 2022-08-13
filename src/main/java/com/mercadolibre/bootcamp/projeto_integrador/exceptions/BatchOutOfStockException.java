package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public class BatchOutOfStockException extends CustomException {
    /**
     * Lança uma CustomException com HTTP Status 400.
     * @throws CustomException
     * @param id
     */
    public BatchOutOfStockException(long id) {
        super("Batch", "Batch with id " + id + " is out of stock", HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }
}
