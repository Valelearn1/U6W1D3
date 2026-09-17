package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utente registrato. Implementa UserDetails cosi' Spring Security puo' usarlo
 * direttamente come "principal": nei controller basta @AuthenticationPrincipal
 * User per avere l'utente loggato.
 *
 * <p>Attenzione: getUsername() restituisce l'EMAIL, perche' e' quella che
 * identifica l'utente al login. Il nome visualizzato e' displayName.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    /** Hash BCrypt, mai la password in chiaro. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected User() {
        // richiesto da JPA
    }

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // ---------- UserDetails ----------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ---------- getter/setter ----------

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
