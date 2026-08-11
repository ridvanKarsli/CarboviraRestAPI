package com.example.carbovirarestapi.common.exception;

/**
 * Bean validation'ın yakalayamayacağı, domain'e özgü iş kuralı ihlallerinde
 * fırlatılır (ör. "kendi ilanınıza mesaj gönderemezsiniz"). HTTP 400'e çevrilir.
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
