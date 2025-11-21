package io.github.asyncbtd.sap.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
public record ImageInfo(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("content_type")
        String contentType,
        @JsonProperty("size")
        Long size,
        @JsonProperty("description")
        String description,
        @JsonProperty("uploaded_by_user")
        User uploadedByUser,
        @JsonProperty("date_time_upload")
        LocalDateTime dateTimeUpload,
        @JsonProperty("available_formats")
        List<String> availableFormats
) {
}
