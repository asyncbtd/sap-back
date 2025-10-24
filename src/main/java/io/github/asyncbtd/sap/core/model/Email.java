package io.github.asyncbtd.sap.core.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record Email(
        String address,
        boolean active
) {
}
