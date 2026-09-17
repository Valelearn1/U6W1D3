package com.example.demo.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce le eccezioni in JSON con un campo "message":
 * e' quello che il frontend legge in client.js per mostrare l'errore a video.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> emailTaken(EmailAlreadyUsedException e) {
        return body(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Validazione fallita sui DTO annotati con @Valid: raccogliamo i messaggi. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        String messages = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElse("Dati non validi.");
        return body(HttpStatus.BAD_REQUEST, messages);
    }

    /**
     * Password sbagliata o email inesistente: stesso messaggio generico per
     * entrambi i casi, altrimenti si potrebbe scoprire quali email sono
     * registrate provandole una a una.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> badCredentials(Exception e) {
        return body(HttpStatus.UNAUTHORIZED, "Email o password non corretti.");
    }

    @ExceptionHandler(OpenRouterException.class)
    public ResponseEntity<Map<String, Object>> openRouter(OpenRouterException e) {
        // 502: il nostro server sta bene, e' il servizio a monte che non ha risposto.
        return body(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception e) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR,
                e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message == null ? status.getReasonPhrase() : message));
    }
}
