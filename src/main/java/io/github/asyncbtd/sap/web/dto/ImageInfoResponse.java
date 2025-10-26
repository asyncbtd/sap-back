package io.github.asyncbtd.sap.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record ImageInfoResponse(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("content_type")
        String contentType,
        @JsonProperty("size")
        Long size,
        @JsonProperty("description")
        String description,
        @JsonProperty("uploaded_by_user")
        String uploadedByUser,
        @JsonProperty("date_time_upload")
        LocalDateTime dateTimeUpload
) {
}
