package io.github.asyncbtd.sap.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder(toBuilder = true)
public record TokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("expires_in")
        Long expiresIn,
        @JsonProperty("refresh_expires_in")
        Long refreshExpiresIn
) {
}
