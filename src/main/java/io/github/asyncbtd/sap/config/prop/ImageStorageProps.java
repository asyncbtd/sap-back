package io.github.asyncbtd.sap.config.prop;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Validated
@ConfigurationProperties("sap.image-storage")
public class ImageStorageProps {

    @Getter
    public enum SupportedFormat {
        PNG("png", "image/png", ".png"),
        JPEG("jpeg", "image/jpeg", ".jpg"),
        WEBP("webp", "image/webp", ".webp"),
        GIF("gif", "image/gif", ".gif"),
        BMP("bmp", "image/bmp", ".bmp"),
        TIFF("tiff", "image/tiff", ".tiff"),
        ICO("ico", "image/x-icon", ".ico"),
        SVG("svg", "image/svg+xml", ".svg");

        private final String formatName;
        private final String mimeType;
        private final String extension;

        SupportedFormat(String formatName, String mimeType, String extension) {
            this.formatName = formatName;
            this.mimeType = mimeType;
            this.extension = extension;
        }

//        public String getFormatName() {
//            return formatName;
//        }
//
//        public String getMimeType() {
//            return mimeType;
//        }
//
//        public String getExtension() {
//            return extension;
//        }

        public static SupportedFormat fromMimeType(String mimeType) {
            if (mimeType == null) return null;
            String normalized = mimeType.toLowerCase();
            for (SupportedFormat format : values()) {
                if (format.mimeType.equals(normalized)) {
                    return format;
                }
            }
            return null;
        }

        public static boolean isSupportedMimeType(String mimeType) {
            return fromMimeType(mimeType) != null;
        }
    }

    /// Path to save images on disk
    @NotNull
    private String path;

    /// Max size file
    private DataSize maxSize = DataSize.ofMegabytes(15L);

    @Getter
    @Setter
    public static class SaveFormatsConfig {
        private Boolean png;
        private Boolean jpeg;
        private Boolean webp;
        private Boolean gif;
        private Boolean bmp;
        private Boolean tiff;
        private Boolean ico;
        private Boolean svg;
    }

    private SaveFormatsConfig supportedFormats = new SaveFormatsConfig();

    /**
     * Проверяет, является ли формат поддерживаемым для загрузки
     */
    public boolean isSupportedFormat(String mimeType) {
        return SupportedFormat.isSupportedMimeType(mimeType);
    }

    /**
     * Проверяет, должен ли формат сохраняться
     */
    public boolean shouldSaveFormat(String mimeType) {
        if (supportedFormats == null) return true;

        SupportedFormat format = SupportedFormat.fromMimeType(mimeType);
        if (format == null) return false;

        return switch (format.formatName.toLowerCase()) {
            case "png" -> supportedFormats.png;
            case "jpeg" -> supportedFormats.jpeg;
            case "webp" -> supportedFormats.webp;
            case "gif" -> supportedFormats.gif;
            case "bmp" -> supportedFormats.bmp;
            case "tiff" -> supportedFormats.tiff;
            case "ico" -> supportedFormats.ico;
            case "svg" -> supportedFormats.svg;
            default -> false;
        };
    }

    /**
     * Получает список MIME типов для выходных форматов (только включенные)
     */
    public List<String> getOutputMimeTypes() {
        List<String> formats = new ArrayList<>();

        if (supportedFormats.png) {
            formats.add(SupportedFormat.PNG.getMimeType());
        }
        if (supportedFormats.jpeg) {
            formats.add(SupportedFormat.JPEG.getMimeType());
        }
        if (supportedFormats.webp) {
            formats.add(SupportedFormat.WEBP.getMimeType());
        }
        if (supportedFormats.gif) {
            formats.add(SupportedFormat.GIF.getMimeType());
        }
        if (supportedFormats.bmp) {
            formats.add(SupportedFormat.BMP.getMimeType());
        }
        if (supportedFormats.tiff) {
            formats.add(SupportedFormat.TIFF.getMimeType());
        }
        if (supportedFormats.ico) {
            formats.add(SupportedFormat.ICO.getMimeType());
        }
        if (supportedFormats.svg) {
            formats.add(SupportedFormat.SVG.getMimeType());
        }

        return formats;
    }

    /**
     * Получает список расширений для выходных форматов (только включенные)
     */
    public List<String> getOutputExtensions() {
        List<String> extensions = new ArrayList<>();

        if (supportedFormats.png) {
            extensions.add(SupportedFormat.PNG.getExtension());
        }
        if (supportedFormats.jpeg) {
            extensions.add(SupportedFormat.JPEG.getExtension());
        }
        if (supportedFormats.webp) {
            extensions.add(SupportedFormat.WEBP.getExtension());
        }
        if (supportedFormats.gif) {
            extensions.add(SupportedFormat.GIF.getExtension());
        }
        if (supportedFormats.bmp) {
            extensions.add(SupportedFormat.BMP.getExtension());
        }
        if (supportedFormats.tiff) {
            extensions.add(SupportedFormat.TIFF.getExtension());
        }
        if (supportedFormats.ico) {
            extensions.add(SupportedFormat.ICO.getExtension());
        }
        if (supportedFormats.svg) {
            extensions.add(SupportedFormat.SVG.getExtension());
        }

        return extensions;
    }
}
