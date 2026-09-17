package com.example.demo.security;

import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Dice a Spring Security come trovare un utente a partire dall'email.
 * La nostra entita' User implementa gia' UserDetails, quindi la restituiamo
 * cosi' com'e'.
 *
 * <p>Sta in una classe sua e non dentro SecurityConfig di proposito: il
 * JwtAuthenticationFilter ha bisogno di questo bean, e SecurityConfig ha
 * bisogno del filtro. Tenendoli insieme si creerebbe una dipendenza circolare
 * e il contesto non partirebbe.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Nessun utente con email " + email));
    }
}
