package io.github.asyncbtd.sap.core;

import io.github.asyncbtd.sap.core.model.Email;
import io.github.asyncbtd.sap.core.model.User;
import io.github.asyncbtd.sap.web.dto.TokenResponse;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class KeycloakMapper {

    public User toUser(UserRepresentation userRepresentation) {
        if (userRepresentation == null) {
            return null;
        }

        var email = Email.builder()
                .address(userRepresentation.getEmail())
                .active(userRepresentation.isEmailVerified())
                .build();
        return User.builder()
                .id(UUID.fromString(userRepresentation.getId()))
                .username(userRepresentation.getUsername())
                .email(email)
                .build();
    }

    public UserRepresentation toUserRepresentation(User user) {
        if (user == null) {
            return null;
        }

        var credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(user.password());
        credential.setTemporary(false);

        var keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(user.username());
        keycloakUser.setCredentials(List.of(credential));
        keycloakUser.setRequiredActions(List.of());
        keycloakUser.setEnabled(true);

        if (user.email() != null && user.email().active()) {
            keycloakUser.setEmail(user.email().address());
            keycloakUser.setEmailVerified(true);
        }

        return keycloakUser;
    }

    public TokenResponse toTokenResponse(AccessTokenResponse accessTokenResponse) {
        if (accessTokenResponse == null) {
            return null;
        }

        return TokenResponse.builder()
                .accessToken(accessTokenResponse.getToken())
                .refreshToken(accessTokenResponse.getRefreshToken())
                .expiresIn(accessTokenResponse.getExpiresIn())
                .refreshExpiresIn(accessTokenResponse.getRefreshExpiresIn())
                .build();
    }
}
