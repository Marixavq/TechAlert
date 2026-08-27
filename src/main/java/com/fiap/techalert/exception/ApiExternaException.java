package com.fiap.techalert.exception;

public class ApiExternaException extends RuntimeException {

    public ApiExternaException(String mensagem) {
        super(mensagem);
    }

    public ApiExternaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
