package io.github.asyncbtd.sap.web.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record ImageInfo(
        UUID id,
        String filename,
        Long size, String contentType,
        LocalDateTime uploadedAt
) {
}

