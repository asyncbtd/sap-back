package io.github.asyncbtd.sap.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotNull(message = "Username is required")
        @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
        String username,

        @NotNull(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
