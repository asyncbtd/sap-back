package io.github.asyncbtd.sap.config;

import io.github.asyncbtd.sap.config.prop.KeycloakProps;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakProps props;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(props.getServerUrl())
                .realm(props.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .build();
    }

    @Bean
    public RealmResource keycloakRealm(Keycloak keycloak) {
        return keycloak.realm(props.getRealm());
    }
}
