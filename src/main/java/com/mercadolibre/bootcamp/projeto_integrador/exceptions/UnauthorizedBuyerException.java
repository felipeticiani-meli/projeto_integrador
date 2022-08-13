package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class UnauthorizedBuyerException extends CustomException {
    /**
     * Lança uma CustomException com HTTP Status 401.
     * @param buyerId buyer id
     * @param purchaseId purchaseorder id
     */
    public UnauthorizedBuyerException(long buyerId, long purchaseId) {
        super("Id " + buyerId + " is not authorized.", "Id " + buyerId + " is not authorized to perform actions in purchaseorder " + purchaseId + ".", HttpStatus.UNAUTHORIZED, LocalDateTime.now());
    }
}
