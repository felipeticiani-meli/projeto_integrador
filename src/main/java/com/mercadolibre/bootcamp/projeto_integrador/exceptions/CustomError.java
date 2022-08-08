package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CustomError {
    private String name;
    private String message;
    private LocalDateTime timestamp;

    public CustomError(CustomException exception) {
        this.name = exception.getName();
        this.message = exception.getMessage();
        this.timestamp = exception.getTimestamp();
    }
}
