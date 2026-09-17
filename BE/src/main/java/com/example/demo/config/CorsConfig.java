package com.example.demo.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Senza questa configurazione il browser blocca ogni chiamata del frontend:
 * Vite gira su :5173 e il backend su :8080, quindi sono due origini diverse.
 *
 * <p>E' esposta come CorsConfigurationSource (e non come WebMvcConfigurer)
 * perche' ora le richieste passano prima dalla catena di filtri di Spring
 * Security: il CORS va applicato li', altrimenti il preflight OPTIONS verrebbe
 * respinto con 401 prima ancora di arrivare al controller.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(
                List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
