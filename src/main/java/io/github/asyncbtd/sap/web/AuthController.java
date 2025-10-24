package io.github.asyncbtd.sap.web;

import io.github.asyncbtd.sap.core.service.AuthService;
import io.github.asyncbtd.sap.core.service.UserService;
import io.github.asyncbtd.sap.web.dto.LoginRequest;
import io.github.asyncbtd.sap.web.dto.TokenResponse;
import io.github.asyncbtd.sap.web.dto.LogoutRequest;
import io.github.asyncbtd.sap.web.dto.RefreshTokenRequest;
import io.github.asyncbtd.sap.web.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerReq
    ) {
        userService.registerUser(registerReq);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest loginReq
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.login(loginReq));
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenReq) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.refresh(refreshTokenReq));
    }

    @GetMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> logout(
            @RequestBody LogoutRequest logoutReq
    ) {
        authService.logout(logoutReq);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}
