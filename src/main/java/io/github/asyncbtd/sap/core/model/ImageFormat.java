package io.github.asyncbtd.sap.core.model;

import io.github.asyncbtd.sap.core.exception.BadRequestHttpException;
import lombok.Getter;

@Getter
public enum ImageFormat {
    GIF(".gif", "image/gif"),
    PNG(".png", "image/png");

    private final String extension;
    private final String contentType;

    ImageFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public static ImageFormat fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return PNG;
        }

        return switch (mimeType.toLowerCase()) {
            case "image/gif" -> GIF;
            case "image/png", "image/jpeg", "image/jpg", "image/jpe" -> PNG;
            default -> throw new BadRequestHttpException("Unsupported file extension: " + mimeType.toLowerCase());
        };
    }
}
