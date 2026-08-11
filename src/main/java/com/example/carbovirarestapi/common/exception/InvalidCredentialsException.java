package com.example.carbovirarestapi.common.exception;

/** Giriş bilgileri hatalıysa fırlatılır. GlobalExceptionHandler bunu HTTP 401'e çevirir. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
