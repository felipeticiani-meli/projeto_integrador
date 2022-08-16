package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class UnauthorizedManagerException extends CustomException {
    /**
     * Lança uma CustomException com HTTP Status 403.
     * @param managerName manager name
     */
    public UnauthorizedManagerException(String managerName) {
        super(managerName + " is not authorized.", managerName + " is not authorized to perform this action.", HttpStatus.FORBIDDEN, LocalDateTime.now());
    }
}
