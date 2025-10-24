package io.github.asyncbtd.sap.web.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record ErrorResponse(
        String error,
        String message,
        String description
) {
}
