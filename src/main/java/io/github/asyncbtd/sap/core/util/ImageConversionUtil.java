package io.github.asyncbtd.sap.core.util;

import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageConversionUtil {

    /**
     * Конвертирует BufferedImage в целевой формат
     * 
     * @param originalImage исходное изображение
     * @param targetMimeType целевой MIME тип (например image/jpeg)
     * @return BufferedImage сконвертированного изображения
     * @throws IOException если не удалось конвертировать
     */
    public BufferedImage convert(BufferedImage originalImage, String targetMimeType) throws IOException {
        ImageStorageProps.SupportedFormat format = ImageStorageProps.SupportedFormat.fromMimeType(targetMimeType);
        if (format == null) {
            throw new IOException("Unsupported target format: " + targetMimeType);
        }
        
        String formatName = format.getFormatName().toUpperCase();
        
        log.debug("Converting image to format: {}", formatName);
        
        // Для некоторых форматов может потребоваться конвертация типа BufferedImage
        return convertIfNeeded(originalImage, formatName);
    }

    /**
     * Конвертирует BufferedImage в другой тип, если необходимо для целевого формата
     */
    private BufferedImage convertIfNeeded(BufferedImage image, String targetFormat) {
        // JPEG не поддерживает прозрачность, конвертируем в RGB
        if ("JPG".equals(targetFormat) || "JPEG".equals(targetFormat)) {
            if (image.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage newImage = new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                newImage.getGraphics().drawImage(image, 0, 0, null);
                return newImage;
            }
        }
        
        // Для других форматов возвращаем как есть
        return image;
    }

    /**
     * Проверяет, поддерживается ли формат ImageIO
     */
    public boolean isFormatSupported(String extension) {
        String formatName = extension.substring(1).toUpperCase();
        String[] writerFormats = ImageIO.getWriterFormatNames();
        
        for (String format : writerFormats) {
            if (format.equalsIgnoreCase(formatName)) {
                return true;
            }
        }
        return false;
    }
}


