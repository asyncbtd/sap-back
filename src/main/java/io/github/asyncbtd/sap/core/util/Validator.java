package io.github.asyncbtd.sap.core.util;

import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.core.exception.BadRequestHttpException;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class Validator {

    private static ImageStorageProps props;

    public static void init(ImageStorageProps imageStorageProps) {
        props = imageStorageProps;
    }

    public static void validateImage(MultipartFile file) {
        validateNotEmpty(file);
        validateIsImage(file);
        validateSupportedFormat(file);
        validateFileSize(file);
    }

    private static void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestHttpException("File is empty");
        }
    }

    private static void validateIsImage(MultipartFile file) {
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestHttpException("The file is not an image");
        }
    }

    private static void validateSupportedFormat(MultipartFile file) {
        String contentType = file.getContentType();
        String formatName = getFormatNameFromMimeType(contentType);

        // Для оригинальных форматов (GIF, BMP, TIFF, ICO, SVG) проверяем конфиг
        // Для конвертируемых (PNG, JPEG, WEBP) разрешаем всегда
        if (ImageFormatUtil.isOriginalFormat(formatName)) {
            if (!props.isSupportedFormat(contentType)) {
                throw new BadRequestHttpException(
                        String.format("Unsupported image format: %s", contentType)
                );
            }

            if (!props.shouldSaveFormat(contentType)) {
                throw new BadRequestHttpException(
                        String.format("Image format %s is not enabled in configuration. " +
                                "Enable it in sap.image-storage.save-formats.%s configuration",
                                contentType, formatName)
                );
            }
        }
        // Конвертируемые форматы всегда разрешены
    }

    private static String getFormatNameFromMimeType(String mimeType) {
        if (mimeType == null) return "unknown";

        return switch (mimeType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpeg";
            case "image/jpg" -> "jpeg";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "image/bmp" -> "bmp";
            case "image/tiff" -> "tiff";
            case "image/x-icon" -> "ico";
            case "image/svg+xml" -> "svg";
            default -> "unknown";
        };
    }

    private static void validateFileSize(MultipartFile file) {
        var maxSize = props.getMaxSize();
        if (file.getSize() > maxSize.toBytes()) {
            throw new BadRequestHttpException(
                    String.format("The file size exceeds %d MB", maxSize.toMegabytes())
            );
        }
    }
}
