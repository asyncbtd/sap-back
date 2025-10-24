package io.github.asyncbtd.sap.config;

import io.github.asyncbtd.sap.config.prop.KeycloakProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";

    private final KeycloakProps keycloakProps;
    private final RestTemplate restTemplate;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(AbstractHttpConfigurer::disable);
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
                    "/", "/index.html",
                    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                    "/api/v1/auth/login/**", "/api/v1/auth/register/**"
            ).permitAll();
            auth.anyRequest().authenticated();
        });

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );
        return http.build();
    }

    // Changing JwtDecoder because the default one from Spring Boot has too small a timeout
    @Bean
    public JwtDecoder jwtDecoder() {
        var jwkSetUri = keycloakProps.getServerUrl() + "/realms/" + keycloakProps.getRealm() + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }

    // TODO refact token data extraction 24.10.2025
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        try {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
            log.debug("JWT Authentication Converter configured successfully");
            return converter;
        } catch (Exception e) {
            log.error("Error creating JWT Authentication Converter: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create JWT Authentication Converter", e);
        }
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            try {
                log.debug("Processing JWT token to extract roles");

                // Extract roles from realm_access
                List<String> realmRoles = extractRealmRoles(jwt);

                // Extract roles from resource_access
                List<String> resourceRoles = extractResourceRoles(jwt);

                // Combine and convert roles to GrantedAuthority
                Collection<GrantedAuthority> authorities = Stream.concat(
                                realmRoles.stream(),
                                resourceRoles.stream()
                        )
                        .filter(role -> role != null && !role.trim().isEmpty())
                        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.trim()))
                        .distinct()
                        .toList();

                log.debug("Extracted {} roles from JWT token", authorities.size());
                return authorities;

            } catch (Exception e) {
                log.error("Error extracting roles from JWT token: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        };
    }

    /// Extracts roles from realm_access claim of JWT token
    @SuppressWarnings("unchecked")
    private List<String> extractRealmRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
            if (realmAccess == null) {
                log.debug("realm_access claim not found in JWT token");
                return Collections.emptyList();
            }

            Object rolesObj = realmAccess.get(ROLES_CLAIM);
            if (rolesObj instanceof List<?>) {
                List<String> roles = (List<String>) rolesObj;
                log.debug("Found {} realm roles", roles.size());
                return roles;
            }

            log.debug("realm_access.roles is not a list or empty");
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error extracting realm roles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /// Extracts roles from resource_access claim of JWT token for specific client
    @SuppressWarnings("unchecked")
    private List<String> extractResourceRoles(Jwt jwt) {
        try {
            Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
            if (resourceAccess == null) {
                log.debug("resource_access claim not found in JWT token");
                return Collections.emptyList();
            }

            String clientId = keycloakProps.getClientId();
            Object clientAccessObj = resourceAccess.get(clientId);
            if (!(clientAccessObj instanceof Map<?, ?>)) {
                log.debug("resource_access.{} not found or not an object", clientId);
                return Collections.emptyList();
            }

            Map<String, Object> clientAccess = (Map<String, Object>) clientAccessObj;
            Object rolesObj = clientAccess.get(ROLES_CLAIM);

            if (rolesObj instanceof List<?>) {
                List<String> roles = (List<String>) rolesObj;
                log.debug("Found {} resource roles for client {}", roles.size(), clientId);
                return roles;
            }

            log.debug("resource_access.{}.roles is not a list or empty", clientId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Error extracting resource roles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
