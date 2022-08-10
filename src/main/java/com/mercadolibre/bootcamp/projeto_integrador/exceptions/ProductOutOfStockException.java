package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ProductOutOfStockException extends CustomException {
    /**
     * Lança uma CustomException com HTTP Status 400.
     * @throws CustomException
     * @param name
     */
    public ProductOutOfStockException(String name) {
        super(name, name + " is out of stock", HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }
}
