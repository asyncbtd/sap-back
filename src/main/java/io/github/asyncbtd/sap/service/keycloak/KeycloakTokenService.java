package io.github.asyncbtd.sap.service.keycloak;

import io.github.asyncbtd.sap.config.prop.KeycloakProps;
import io.github.asyncbtd.sap.core.exception.InternalErrorHttpException;
import io.github.asyncbtd.sap.core.exception.UnauthorizedHttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTokenService { // TODO refact service 24.10.2025

    private final RestTemplate restTemplate;
    private final KeycloakProps props;

    public AccessTokenResponse authenticate(String username, String password) {
        try {
            var tokenUrl = buildTokenUrl();
            var req = buildTokenRequest(username, password);
            var res = restTemplate.postForEntity(
                    tokenUrl, req, AccessTokenResponse.class
            );

            if (res.getStatusCode() != HttpStatus.OK && res.getBody() == null) {
                throw new UnauthorizedHttpException("Authentication failed");

            }
            return res.getBody();
        } catch (Exception e) {
            log.error("Authentication error for user: {}", username, e);
            throw new RuntimeException("Authentication service error: " + e.getMessage(), e);
        }
    }

    public AccessTokenResponse refreshToken(String refreshToken) {
        try {
            var tokenUrl = buildTokenUrl();
            var request = buildRefreshRequest(refreshToken);

            ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl, request, AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Token refreshed successfully");
                return response.getBody();
            }

            throw new UnauthorizedHttpException("Token refresh failed");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Invalid refresh token");
            throw new UnauthorizedHttpException("Invalid or expired refresh token");
        } catch (Exception e) {
            log.error("Token refresh error", e);
            throw new RuntimeException("Token refresh service error: " + e.getMessage(), e);
        }
    }

    public void logout(String refreshToken) {
        try {
            var logoutUrl = buildLogoutUrl();
            var request = buildLogoutRequest(refreshToken);

            restTemplate.postForEntity(logoutUrl, request, String.class);
        } catch (Exception e) {
            throw new InternalErrorHttpException("Logout failed: " + e.getMessage(), e);
        }
    }

    private String buildTokenUrl() {
        return String.format("%s/realms/%s/protocol/openid-connect/token",
                props.getServerUrl(), props.getRealm());
    }

    private String buildLogoutUrl() {
        return String.format("%s/realms/%s/protocol/openid-connect/logout",
                props.getServerUrl(), props.getRealm());
    }

    private HttpEntity<MultiValueMap<String, String>> buildTokenRequest(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", props.getClientId());
        body.add("client_secret", props.getClientSecret());
        body.add("username", username);
        body.add("password", password);

        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<MultiValueMap<String, String>> buildRefreshRequest(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", props.getClientId());
        body.add("client_secret", props.getClientSecret());
        body.add("refresh_token", refreshToken);

        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<MultiValueMap<String, String>> buildLogoutRequest(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", props.getClientId());
        body.add("client_secret", props.getClientSecret());
        body.add("refresh_token", refreshToken);

        return new HttpEntity<>(body, headers);
    }
}
