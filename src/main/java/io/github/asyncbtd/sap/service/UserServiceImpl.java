package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.core.KeycloakMapper;
import io.github.asyncbtd.sap.core.model.Email;
import io.github.asyncbtd.sap.core.model.User;
import io.github.asyncbtd.sap.core.service.UserService;
import io.github.asyncbtd.sap.service.keycloak.KeycloakAdminService;
import io.github.asyncbtd.sap.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
