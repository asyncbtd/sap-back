package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.core.KeycloakMapper;
import io.github.asyncbtd.sap.core.service.AuthService;
import io.github.asyncbtd.sap.service.keycloak.KeycloakTokenService;
import io.github.asyncbtd.sap.web.dto.LoginRequest;
import io.github.asyncbtd.sap.web.dto.RefreshTokenRequest;
import io.github.asyncbtd.sap.web.dto.TokenResponse;
import io.github.asyncbtd.sap.web.dto.LogoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakTokenService keycloakTokenService;
    private final KeycloakMapper keycloakMapper;

    @Override
    public TokenResponse login(LoginRequest loginReq) {
        var tokenResponse = keycloakTokenService.authenticate(
                loginReq.username(),
                loginReq.password()
        );
        return keycloakMapper.toTokenResponse(tokenResponse);
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest refreshTokenReq) {
        var newTokens = keycloakTokenService.refreshToken(refreshTokenReq.refreshToken());
        return keycloakMapper.toTokenResponse(newTokens);
    }

    @Override
    public void logout(LogoutRequest logoutReq) {
        keycloakTokenService.logout(logoutReq.refreshToken());
    }
}
