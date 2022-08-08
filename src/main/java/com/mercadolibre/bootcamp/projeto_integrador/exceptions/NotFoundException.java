package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class NotFoundException extends CustomException {
    /**
     * Throw CustomException with HTTP Status 404.
     * @throws CustomException
     * @param name
     */
    public NotFoundException(String name) {
        super(name + " not found.", "There is no " + name.toLowerCase() + " with the specified id", HttpStatus.NOT_FOUND, LocalDateTime.now());
    }
}
