package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Crea e verifica i token JWT.
 *
 * <p>Un JWT e' composto da tre parti separate da punto: header, payload e
 * firma. Il payload NON e' cifrato, solo codificato in base64: chiunque puo'
 * leggerlo. Quello che nessuno puo' fare senza il segreto e' <em>falsificare la
 * firma</em>, ed e' questo che rende il token affidabile. Quindi dentro non si
 * mettono mai dati sensibili.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long validityMillis;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes) {

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret deve essere lungo almeno 32 caratteri "
                            + "(richiesto da HMAC-SHA256).");
        }
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(bytes);
        this.validityMillis = expirationMinutes * 60_000;
    }

    /** Genera il token per un utente appena autenticato. */
    public String generateToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validityMillis))
                .signWith(key)
                .compact();
    }

    /**
     * Restituisce l'email contenuta nel token, oppure null se il token e'
     * scaduto, manomesso o illeggibile.
     */
    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getValidityMillis() {
        return validityMillis;
    }
}
