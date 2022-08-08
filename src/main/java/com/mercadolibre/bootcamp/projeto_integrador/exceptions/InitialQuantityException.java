package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class InitialQuantityException extends CustomException {
    public InitialQuantityException(int newInitialQuantity, int selledProductsQuantity) {
        super("Invalid batch quantity", "Couldn't update batch initial quantity. New initial quantity: " + newInitialQuantity +
               ". Selled products: " + selledProductsQuantity , HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }
}
