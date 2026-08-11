package com.example.carbovirarestapi.common.exception;

/** İstenen kayıt bulunamadığında fırlatılır. GlobalExceptionHandler bunu HTTP 404'e çevirir. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
