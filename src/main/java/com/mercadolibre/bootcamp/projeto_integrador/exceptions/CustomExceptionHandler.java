package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * Lança exceções não mapeadas, que serão verificadas posterioremente para possível customização de exception.
     * @throws Exception
     * @param exception
     */
//    @ExceptionHandler(Exception.class)
//    public Object unmappedExceptionHandler(Exception exception) {
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new CustomError("Internal Server Error",
//                        "An internal server error has occurred.", LocalDateTime.now()));
//    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomError> sectionNotFoundHandler(NotFoundException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new CustomError(exception));
    }

    @ExceptionHandler(MaxSizeException.class)
    public ResponseEntity<CustomError> maxSizeBatchHandler(MaxSizeException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new CustomError(exception));
    }

    @ExceptionHandler(InitialQuantityException.class)
    public ResponseEntity<CustomError> batchInitialQuantityExceptionHandler(InitialQuantityException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new CustomError(exception));
    }

}
