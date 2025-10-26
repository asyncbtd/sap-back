package io.github.asyncbtd.sap.core.model;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record User(
        UUID id,
        String username,
        String password,
        Email email
) {
}
