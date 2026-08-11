package com.example.carbovirarestapi.common.exception;

/** Benzersiz olması gereken bir alan (e-posta, vergi no vb.) zaten kayıtlıysa fırlatılır. HTTP 409'a çevrilir. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
