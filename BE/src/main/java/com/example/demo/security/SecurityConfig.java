package com.example.demo.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CorsConfigurationSource corsSource;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            // Anche mvcHandlerMappingIntrospector implementa CorsConfigurationSource:
            // senza @Qualifier Spring non saprebbe quale dei due usare.
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsSource) {
        this.jwtFilter = jwtFilter;
        this.corsSource = corsSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsSource))
                // Niente CSRF: non usiamo cookie di sessione, il token viaggia
                // nell'header Authorization e va richiesto esplicitamente dal JS,
                // quindi l'attacco che il CSRF previene qui non si applica.
                .csrf(csrf -> csrf.disable())
                // Niente sessioni lato server: ogni richiesta porta il suo token.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // registrazione e login devono essere raggiungibili da sloggati
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // l'elenco dei modelli serve anche prima del login
                        .requestMatchers("/api/models").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Di default Spring Security risponderebbe con una pagina di login HTML.
     * Qui siamo un'API: serve un 401 con un JSON che il frontend sa leggere.
     */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            write(response, "{\"status\":401,\"message\":\"Devi effettuare l'accesso.\"}");
        };
    }

    private void write(jakarta.servlet.http.HttpServletResponse response, String body)
            throws IOException {
        response.getWriter().write(body);
    }

    /**
     * BCrypt: le password non vengono mai salvate in chiaro e nemmeno cifrate,
     * ma trasformate in un hash non reversibile con un "sale" casuale.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
