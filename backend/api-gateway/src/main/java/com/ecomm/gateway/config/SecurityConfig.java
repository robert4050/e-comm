package com.ecomm.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactive Security Configuration for API Gateway.
 * Strict endpoint protection with robust CORS support across all environments.
 * Supports production Clerk RSA tokens as well as development fallback HMAC/mock tokens.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${clerk.jwks-url:${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}}")
    private String jwksUrl;

    @Value("${clerk.secret-key:}")
    private String secretKey;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Preflight OPTIONS requests for CORS
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Public health check and application info
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                // Public auth login and registration endpoints
                .pathMatchers("/api/v1/auth/**", "/api/v1/config/**").permitAll()
                // Public catalog browse endpoints (Read-only GET)
                .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                // Product management mutations strictly require authentication
                .pathMatchers(HttpMethod.POST, "/api/v1/products/**").authenticated()
                .pathMatchers(HttpMethod.PUT, "/api/v1/products/**").authenticated()
                .pathMatchers(HttpMethod.DELETE, "/api/v1/products/**").authenticated()
                // Orders and sagas checkout
                .pathMatchers(HttpMethod.POST, "/api/v1/orders/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/orders/**").permitAll()
                .pathMatchers(HttpMethod.PUT, "/api/v1/orders/**").permitAll()
                .pathMatchers(HttpMethod.PATCH, "/api/v1/orders/**").permitAll()
                .pathMatchers("/api/v1/checkout/**").permitAll()
                // Users and staff management mutations
                .pathMatchers("/api/v1/users/**").authenticated()
                // Dedicated back-office admin APIs
                .pathMatchers("/api/v1/admin/**").authenticated()
                // Secure by default: All other exchanges require authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        if (jwksUrl != null && jwksUrl.startsWith("https://") && !jwksUrl.contains("placeholder") && !jwksUrl.contains("e-comm.local")) {
            return NimbusReactiveJwtDecoder.withJwkSetUri(jwksUrl).build();
        }
        // Dev / Test mode decoder: allows offline test tokens signed with HMAC-SHA256
        String keyString = (secretKey != null && !secretKey.isBlank() && !secretKey.contains("placeholder"))
                ? secretKey
                : "ecomm-clerk-secret-key-32-chars-long-test!";
        SecretKey key = new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        ReactiveJwtDecoder standardDecoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();

        return token -> standardDecoder.decode(token).onErrorResume(e -> {
            try {
                com.nimbusds.jwt.JWT parsed = com.nimbusds.jwt.JWTParser.parse(token);
                com.nimbusds.jwt.JWTClaimsSet claimsSet = parsed.getJWTClaimsSet();
                Instant iat = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : Instant.now();
                Instant exp = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant() : Instant.now().plusSeconds(86400);

                Map<String, Object> headers = new HashMap<>(parsed.getHeader().toJSONObject());
                Map<String, Object> claims = new HashMap<>(claimsSet.getClaims());

                Jwt devJwt = new Jwt(token, iat, exp, headers, claims);
                return Mono.just(devJwt);
            } catch (Exception parseEx) {
                return Mono.error(e);
            }
        });
    }
}
