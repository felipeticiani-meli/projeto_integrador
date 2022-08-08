package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {
    private String name;
    private String message;
    private HttpStatus status;
    private LocalDateTime timestamp;

}
