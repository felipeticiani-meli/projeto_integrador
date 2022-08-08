package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@ControllerAdvice
public class CustomExceptionHandler {

    /**
     * Lança exceções não mapeadas com HTTP Status 500, que poderão ser verificadas posterioremente.
     * @throw Exception
     * @param exception
     */
    @ExceptionHandler(Exception.class)
    public Object unmappedExceptionHandler(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomError("Internal Server Error",
                        "An internal server error has occurred.", LocalDateTime.now()));
    }

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

    // Trata as exceções referente às validações (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomError> invalidFieldsHandler(MethodArgumentNotValidException exception) {
        // Cria uma lista com todos os erros da exceção recebida.
        List<FieldError> errors = exception.getBindingResult().getFieldErrors();

        // Instancia um novo CustomError, transformando cada FieldError em String
        CustomError error = new CustomError("Campo(s) inválido(s)", errors.stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" | ")), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Trata as exceções lançadas quando ocorre erro na transformação do JSON recebido em Objeto Java, formato inválido.
    @ExceptionHandler({MismatchedInputException.class, InvalidFormatException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<CustomError> invalidFormatHandler(MismatchedInputException exception) {
        /*
        Através do caminho do erro (path), pega o campo (getFieldName) onde o erro ocorreu. Quando o erro ocorrer
        dentro de uma lista, o replace substituirá o valor "null" por ":" para melhorar a visualização.
         */
        String field = exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining())
                .replace("null", ":");

        // Concatena uma mensagem contendo o nome do campo e o tipo esperado no campo.
        String message = field + ": esperado " + exception.getTargetType().getSimpleName() + ".";

        /*
        Instancia um CustomError, recebendo o simpleName da exception no primeiro parâmetro
        e a mensagem no segundo parâmetro.
         */
        CustomError error = new CustomError("Campo(s) inválido(s)", message, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
