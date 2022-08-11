package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ProductOutOfStockException extends CustomException {
    /**
     * Lança uma CustomException com HTTP Status 400.
     * @throws CustomException
     * @param id
     */
    public ProductOutOfStockException(long id) {
        super("Product", "Product with id " + id + " is out of stock", HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }

    public ProductOutOfStockException(List<Long> ids) {
        super("Product", "Products with ids " + ids.toString() + " are out of stock", HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }
}
