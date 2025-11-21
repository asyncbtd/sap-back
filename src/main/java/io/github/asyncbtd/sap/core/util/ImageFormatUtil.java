package io.github.asyncbtd.sap.core.util;

import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ImageFormatUtil {

    /**
     * Получает список форматов для сохранения
     * Форматы-оригиналы сохраняются как есть, остальные конвертируются
     */
    public static List<String> getOutputFormats(String originalMimeType, ImageStorageProps props) {
        ImageStorageProps.SupportedFormat originalFormat = ImageStorageProps.SupportedFormat.fromMimeType(originalMimeType);
        
        // Для оригинальных форматов (GIF, BMP, TIFF, ICO, SVG) не конвертируем
        if (originalFormat != null && isOriginalFormat(originalFormat.getFormatName())) {
            return List.of(originalMimeType);
        }
        
        // Для остальных используем настройки из конфига
        return props.getOutputMimeTypes();
    }

    /**
     * Проверяет, является ли формат оригинальным (сохраняется как есть без конвертации)
     */
    public static boolean isOriginalFormat(String formatName) {
        return switch (formatName.toLowerCase()) {
            case "gif", "bmp", "tiff", "ico", "svg" -> true;
            default -> false;
        };
    }

    /**
     * Получает расширение из MIME типа для неизвестных форматов
     */
    public static String getExtensionFromMimeType(String mimeType) {
        ImageStorageProps.SupportedFormat format = ImageStorageProps.SupportedFormat.fromMimeType(mimeType);
        if (format != null) {
            return format.getExtension();
        }
        
        // Если формат неизвестен, пытаемся извлечь расширение из MIME типа
        String[] parts = mimeType.split("/");
        if (parts.length == 2) {
            return "." + parts[1].split(";")[0];
        }
        
        return ".bin";
    }
}

