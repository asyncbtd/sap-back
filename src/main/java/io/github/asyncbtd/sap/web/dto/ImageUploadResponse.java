package io.github.asyncbtd.sap.web.dto;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record ImageUploadResponse(
        UUID id,
        String filename,
        Long size,
        String contentType
) {
}

