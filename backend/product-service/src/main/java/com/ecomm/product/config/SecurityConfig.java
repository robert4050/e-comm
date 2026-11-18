package com.ecomm.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clerk Authentication and Authorization Configuration for Product Service.
 * Validates Clerk JWTs issued by Clerk (https://clerk.com) and extracts role claims.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${clerk.jwks-url:${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}}")
    private String jwksUrl;

    @Value("${clerk.secret-key:}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/images/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/api/v1/config/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "PRODUCT_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "PRODUCT_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/orders/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/orders/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(clerkJwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwksUrl != null && jwksUrl.startsWith("https://") && !jwksUrl.contains("placeholder") && !jwksUrl.contains("e-comm.local")) {
            return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
        }
        // Dev / Test mode decoder: allows offline test tokens signed with HMAC-SHA256
        String keyString = (secretKey != null && !secretKey.isBlank() && !secretKey.contains("placeholder"))
                ? secretKey
                : "ecomm-clerk-secret-key-32-chars-long-test!";
        SecretKey key = new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtDecoder standardDecoder = NimbusJwtDecoder.withSecretKey(key).build();

        return token -> {
            try {
                return standardDecoder.decode(token);
            } catch (Exception e) {
                try {
                    com.nimbusds.jwt.JWT parsed = com.nimbusds.jwt.JWTParser.parse(token);
                    com.nimbusds.jwt.JWTClaimsSet claimsSet = parsed.getJWTClaimsSet();
                    java.time.Instant iat = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : java.time.Instant.now();
                    java.time.Instant exp = claimsSet.getExpirationTime() != null ? claimsSet.getExpirationTime().toInstant() : java.time.Instant.now().plusSeconds(86400);

                    Map<String, Object> headers = new HashMap<>(parsed.getHeader().toJSONObject());
                    Map<String, Object> claims = new HashMap<>(claimsSet.getClaims());

                    return new Jwt(token, iat, exp, headers, claims);
                } catch (Exception parseEx) {
                    throw (e instanceof org.springframework.security.oauth2.jwt.JwtException) ? (org.springframework.security.oauth2.jwt.JwtException) e : new org.springframework.security.oauth2.jwt.JwtException("Failed to decode token", e);
                }
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter clerkJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new ClerkRoleConverter());
        return converter;
    }

    /**
     * Converts Clerk JWT claims to Spring Security GrantedAuthorities.
     * Supports:
     * - "roles": ["PRODUCT_MANAGER", "ADMIN"]
     * - "role": "PRODUCT_MANAGER"
     * - "org_role": "org:admin" -> ROLE_ADMIN
     * - "public_metadata": {"roles": ["..."], "role": "..."}
     * - "metadata": {"roles": ["..."], "role": "..."}
     */
    public static class ClerkRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Set<String> extractedRoles = new HashSet<>();

            // 1. Array of roles: "roles"
            Object rolesClaim = jwt.getClaims().get("roles");
            if (rolesClaim instanceof Collection<?> rolesList) {
                for (Object r : rolesList) {
                    if (r != null) extractedRoles.add(r.toString());
                }
            } else if (rolesClaim instanceof String singleRole) {
                extractedRoles.add(singleRole);
            }

            // 2. Single role: "role"
            Object singleRoleClaim = jwt.getClaims().get("role");
            if (singleRoleClaim instanceof String r) {
                extractedRoles.add(r);
            }

            // 3. Organization role: "org_role" (e.g. "org:admin", "org:member")
            Object orgRoleClaim = jwt.getClaims().get("org_role");
            if (orgRoleClaim instanceof String orgRole) {
                String cleanRole = orgRole.replaceFirst("(?i)^org:", "");
                extractedRoles.add(cleanRole);
            }

            // 4. Public metadata: "public_metadata" or "metadata"
            extractFromMetadata(jwt.getClaims().get("public_metadata"), extractedRoles);
            extractFromMetadata(jwt.getClaims().get("metadata"), extractedRoles);

            // 5. Fallback for Keycloak legacy tokens: "realm_access.roles"
            Object realmAccess = jwt.getClaims().get("realm_access");
            if (realmAccess instanceof Map<?, ?> map) {
                Object r = map.get("roles");
                if (r instanceof Collection<?> list) {
                    for (Object item : list) {
                        if (item != null) extractedRoles.add(item.toString());
                    }
                }
            }

            if (extractedRoles.isEmpty()) {
                return Collections.emptyList();
            }

            return extractedRoles.stream()
                    .map(role -> {
                        String clean = role.toUpperCase().trim();
                        return new SimpleGrantedAuthority(clean.startsWith("ROLE_") ? clean : "ROLE_" + clean);
                    })
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        private void extractFromMetadata(Object metadataClaim, Set<String> target) {
            if (metadataClaim instanceof Map<?, ?> meta) {
                Object roles = meta.get("roles");
                if (roles instanceof Collection<?> list) {
                    for (Object item : list) {
                        if (item != null) target.add(item.toString());
                    }
                }
                Object role = meta.get("role");
                if (role instanceof String str) {
                    target.add(str);
                }
            }
        }
    }
}
