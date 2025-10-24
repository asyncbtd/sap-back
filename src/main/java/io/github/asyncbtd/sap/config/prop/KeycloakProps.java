package io.github.asyncbtd.sap.config.prop;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@ConfigurationProperties("sap.keycloak")
public class KeycloakProps {
    @NotNull
    String serverUrl;
    @NotNull
    String realm;
    @NotNull
    String clientId;
    @NotNull
    String clientSecret;
}
