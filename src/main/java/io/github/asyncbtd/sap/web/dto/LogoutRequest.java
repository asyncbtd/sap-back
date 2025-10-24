package io.github.asyncbtd.sap.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogoutRequest(
        @JsonProperty("refresh_token")
        String refreshToken
) {
}
