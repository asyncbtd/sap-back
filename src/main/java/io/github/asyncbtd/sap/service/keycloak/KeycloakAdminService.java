package io.github.asyncbtd.sap.service.keycloak;

import io.github.asyncbtd.sap.core.exception.ConflictHttpException;
import io.github.asyncbtd.sap.core.exception.InternalErrorHttpException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final RealmResource realm;

    public void registerUser(UserRepresentation userRepresentation) {
        try (Response res = realm.users().create(userRepresentation)) {
            if (res.getStatus() == 409) {
                var errorMessage = res.readEntity(ErrorRepresentation.class);
                throw new ConflictHttpException(errorMessage.getErrorMessage());
            }

            if (res.getStatus() >= 400) {
                var errorMessage = res.readEntity(ErrorRepresentation.class).getErrorMessage();
                throw new InternalErrorHttpException("Keycloak error: " + errorMessage);
            }
        }
    }
}
