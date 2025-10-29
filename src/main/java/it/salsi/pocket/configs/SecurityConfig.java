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

    @Value("${security.cors.enable-strict:false}")
    private boolean corsEnableStrict;

    @Value("${security.cors.header-token:__cors_token_change_me__}")
    private String corsHeaderToken;

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
                        .anyRequest().authenticated())

                // Configurazione della sessione - Stateless per API REST
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // HTTP Basic per admin endpoints
                .httpBasic(basic -> basic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setHeader("WWW-Authenticate", "Basic realm=\"Pocket Admin\"");
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        }))

                // Aggiunge il filtro di autenticazione personalizzato
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Headers di sicurezza
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .xssProtection(xss -> {
                        })
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)))

                .build();
    }

    @Bean
    public @NotNull CorsConfigurationSource corsConfigurationSource() {
        final var configuration = new CorsConfiguration();

        if (corsEnableStrict) {
            final var allowedOrigins = new ArrayList<String>();

            allowedOrigins.add(serverUrl);
            allowedOrigins.add(serverUrl.replace("http://", "https://"));

            allowedOrigins.add("http://localhost:*");
            allowedOrigins.add("https://localhost:*");

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

            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

            configuration.setAllowedHeaders(Arrays.asList(
                    "Authorization",
                    "Content-Type",
                    "X-Requested-With",
                    "Accept",
                    "Origin",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers"));

            configuration.setAllowCredentials(true);

            configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));
        } else {
            configuration.addAllowedOriginPattern("*");
            configuration.addAllowedMethod("*");
            configuration.addAllowedHeader("*");
            configuration.setAllowCredentials(true);

            configuration.addExposedHeader("X-API-Key");
        }

        configuration.setMaxAge(3600L);

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey != null && apiKey.equals("your-secret-api-key-here")) {
            // Autenticazione riuscita
            filterChain.doFilter(request, response);
        } else {
            // Autenticazione fallita
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
        }
    }
}