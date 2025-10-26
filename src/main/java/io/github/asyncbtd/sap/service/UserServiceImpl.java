package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.core.KeycloakMapper;
import io.github.asyncbtd.sap.core.exception.InternalErrorHttpException;
import io.github.asyncbtd.sap.core.model.Email;
import io.github.asyncbtd.sap.core.model.User;
import io.github.asyncbtd.sap.core.service.UserService;
import io.github.asyncbtd.sap.service.keycloak.KeycloakAdminService;
import io.github.asyncbtd.sap.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakAdminService keycloakAdminService;
    private final KeycloakMapper keycloakMapper;

    @Override
    public void registerUser(RegisterRequest registerReq) {
        var email = Email.builder()
                .address(registerReq.email())
                .active(true)
                .build();
        var user = User.builder()
                .username(registerReq.username())
                .password(registerReq.password())
                .email(email)
                .build();
        var keycloakUser = keycloakMapper.toUserRepresentation(user);
        keycloakAdminService.registerUser(keycloakUser);
    }

    @Override
    public User getAuthorizedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() == "anonymousUser") {
            return null;
        }

        var jwtToken = (JwtAuthenticationToken) authentication;
        var jwt = (Jwt) jwtToken.getPrincipal();
        var username = jwt.getClaimAsString("preferred_username");

        return keycloakMapper.toUser(keycloakAdminService.getUserByUsername(username));
    }
}
