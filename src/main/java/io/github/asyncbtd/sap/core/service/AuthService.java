package io.github.asyncbtd.sap.core.service;

import io.github.asyncbtd.sap.web.dto.LoginRequest;
import io.github.asyncbtd.sap.web.dto.LogoutRequest;
import io.github.asyncbtd.sap.web.dto.RefreshTokenRequest;
import io.github.asyncbtd.sap.web.dto.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest loginReq);
    TokenResponse refresh(RefreshTokenRequest refreshTokenReq);
    void logout(LogoutRequest logoutReq);
}
