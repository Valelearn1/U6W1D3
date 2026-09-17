package com.example.demo.exception;

/** Qualcosa e' andato storto parlando con OpenRouter (chiave, quota, rete...). */
public class OpenRouterException extends RuntimeException {

    public OpenRouterException(String message) {
        super(message);
    }

    public OpenRouterException(String message, Throwable cause) {
        super(message, cause);
    }
}
