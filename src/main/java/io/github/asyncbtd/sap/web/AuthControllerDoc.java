package io.github.asyncbtd.sap.web;

import io.github.asyncbtd.sap.web.dto.LoginRequest;
import io.github.asyncbtd.sap.web.dto.LogoutRequest;
import io.github.asyncbtd.sap.web.dto.RefreshTokenRequest;
import io.github.asyncbtd.sap.web.dto.RegisterRequest;
import io.github.asyncbtd.sap.web.dto.TokenResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthControllerDoc {

    ResponseEntity<?> register(
            RegisterRequest registerReq
    );

    ResponseEntity<TokenResponse> login(
            LoginRequest loginReq
    );

    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<TokenResponse> refresh(
            RefreshTokenRequest refreshTokenReq
    );

    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<?> logout(
            LogoutRequest logoutReq
    );
}
