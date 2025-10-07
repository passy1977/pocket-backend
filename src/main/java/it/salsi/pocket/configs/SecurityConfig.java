/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/

package it.salsi.pocket.configs;

import it.salsi.pocket.security.AuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;

@Log
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${server.auth.user:admin}")
    private String adminUser;

    @Value("${server.auth.passwd:admin}")
    private String adminPassword;

    @Value("${server.api-version:/api/v5}")
    private String apiVersion;

    @Value("${server.url:http://localhost:8081}")
    private String serverUrl;

    @Value("${security.cors.additional-origins:}")
    private String additionalCorsOrigins;

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public @NotNull PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public @NotNull UserDetailsService userDetailsService() {
        final var admin = User.builder()
                .username(adminUser)
                .password(passwordEncoder().encode(adminPassword))
                .authorities("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public @NotNull SecurityFilterChain filterChain(@NotNull final HttpSecurity http) throws Exception {
        return http
                // Disabilita CSRF per API REST
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configurazione CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configurazione delle autorizzazioni
                .authorizeHttpRequests(auth -> auth
                        // Endpoints pubblici per il controllo della salute
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        
                        // Endpoints API protetti con autenticazione custom
                        .requestMatchers(apiVersion + "/**").hasAuthority("USER")
                        
                        // Admin endpoints protetti con HTTP Basic
                        .requestMatchers("/actuator/**").hasAuthority("ADMIN")
                        
                        // Tutto il resto richiede autenticazione
                        .anyRequest().authenticated()
                )
                
                // Configurazione della sessione - Stateless per API REST
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // HTTP Basic per admin endpoints
                .httpBasic(basic -> basic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setHeader("WWW-Authenticate", "Basic realm=\"Pocket Admin\"");
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                
                // Aggiunge il filtro di autenticazione personalizzato
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                // Headers di sicurezza
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(contentTypeOptions -> {})
                        .xssProtection(xss -> {})
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                        )
                )
                
                .build();
    }

    @Bean
    public @NotNull CorsConfigurationSource corsConfigurationSource() {
        final var configuration = new CorsConfiguration();
        
        // Lista delle origini permesse
        final var allowedOrigins = new ArrayList<String>();
        
        // Aggiungi URL del server configurato
        allowedOrigins.add(serverUrl);
        allowedOrigins.add(serverUrl.replace("http://", "https://"));
        
        // Aggiungi localhost per sviluppo
        allowedOrigins.add("http://localhost:*");
        allowedOrigins.add("https://localhost:*");
        
        // Aggiungi origini aggiuntive dalla configurazione
        if (additionalCorsOrigins != null && !additionalCorsOrigins.trim().isEmpty()) {
            final var additionalOrigins = additionalCorsOrigins.split(",");
            for (final var origin : additionalOrigins) {
                final var trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    allowedOrigins.add(trimmedOrigin);
                }
            }
        }
        
        log.info("CORS allowed origins: " + allowedOrigins);
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Metodi HTTP permessi
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Headers permessi
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", 
                "Content-Type", 
                "X-Requested-With", 
                "Accept", 
                "Origin", 
                "Access-Control-Request-Method", 
                "Access-Control-Request-Headers"
        ));
        
        // Permetti credenziali
        configuration.setAllowCredentials(true);
        
        // Headers esposti al client
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));
        
        // Max age per preflight requests (1 ora)
        configuration.setMaxAge(3600L);

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}