package io.github.asyncbtd.sap.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.core.exception.BadRequestHttpException;
import io.github.asyncbtd.sap.core.exception.InternalErrorHttpException;
import io.github.asyncbtd.sap.core.model.ImageInfo;
import io.github.asyncbtd.sap.core.storage.ImageStorage;
import io.github.asyncbtd.sap.core.util.ImageConversionUtil;
import io.github.asyncbtd.sap.core.util.ImageFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileSystemImageStorage implements ImageStorage {

    private final ObjectMapper objectMapper;
    private final ImageConversionUtil imageConversionUtil;
    private final ImageStorageProps imageStorageProps;
    private final Path storageLocation;

    public FileSystemImageStorage(ObjectMapper objectMapper,
                                   ImageConversionUtil imageConversionUtil,
                                   ImageStorageProps imageStorageProps) {
        this.objectMapper = objectMapper;
        this.imageConversionUtil = imageConversionUtil;
        this.imageStorageProps = imageStorageProps;
        this.storageLocation = Paths.get(imageStorageProps.getPath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for storing images", e);
        }
    }

    @Override
    public void save(ImageInfo imageInfo, MultipartFile file) {
        try {
            String originalMimeType = file.getContentType();
            ImageStorageProps.SupportedFormat originalFormat = ImageStorageProps.SupportedFormat.fromMimeType(originalMimeType);
            
            // Сохраняем содержимое файла в byte array для многократного использования
            byte[] fileBytes = file.getBytes();
            
            List<String> outputFormats;
            
            if (originalFormat == null) {
                // Неизвестный формат - сохраняем оригинал без конвертации
                String originalExtension = ImageFormatUtil.getExtensionFromMimeType(originalMimeType);
                var imagePath = storageLocation.resolve(imageInfo.id() + originalExtension);
                Files.write(imagePath, fileBytes);
                outputFormats = List.of(originalMimeType);
                log.info("Saved unsupported format {} as original", originalMimeType);
            } else {
                outputFormats = ImageFormatUtil.getOutputFormats(originalMimeType, imageStorageProps);
            }

            // Читаем оригинальное изображение из byte array
            var bufferedImage = ImageIO.read(new java.io.ByteArrayInputStream(fileBytes));
            if (bufferedImage == null) {
                throw new InternalErrorHttpException("Failed to read image from input stream");
            }

            // Сохраняем в каждом формате (если это поддержанный формат)
            if (originalFormat != null) {
                // Проверяем, является ли формат оригинальным (не конвертируется)
                if (ImageFormatUtil.isOriginalFormat(originalFormat.getFormatName())) {
                    // Сохраняем оригинальный файл
                    if (imageStorageProps.shouldSaveFormat(originalMimeType)) {
                        String extension = originalFormat.getExtension();
                        var imagePath = storageLocation.resolve(imageInfo.id() + extension);
                        Files.write(imagePath, fileBytes);
                        log.debug("Saved {} in original format at path: {}", originalFormat.getFormatName(), imagePath);
                    }
                } else {
                    // Для конвертируемых форматов (PNG, JPEG, WEBP) создаем версии в разных форматах
                    for (String format : outputFormats) {
                        ImageStorageProps.SupportedFormat supportedFormat = ImageStorageProps.SupportedFormat.fromMimeType(format);
                        if (supportedFormat == null) continue;

                        // Проверяем, должен ли формат сохраняться согласно конфигу
                        if (!imageStorageProps.shouldSaveFormat(format)) {
                            log.debug("Format {} is disabled in config, skipping", format);
                            continue;
                        }

                        String extension = supportedFormat.getExtension();
                        var imagePath = storageLocation.resolve(imageInfo.id() + extension);

                        // Конвертируем в целевой формат
                        var convertedImage = imageConversionUtil.convert(bufferedImage, format);
                        var ioFormat = extension.substring(1).toUpperCase();
                        ImageIO.write(convertedImage, ioFormat, imagePath.toFile());

                        log.debug("Saved image in format: {} at path: {}", format, imagePath);
                    }
                }
            }

            Path jsonPath = storageLocation.resolve(imageInfo.id() + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), imageInfo);

            log.info("Saved image in {} formats and metadata: {}", outputFormats.size(), imageInfo);
        } catch (IOException e) {
            throw new InternalErrorHttpException("Failed to save file", e);
        }
    }

    @Override
    public Resource load(UUID imageId) {
        Path file = findImageById(imageId);

        if (file == null) {
            throw new BadRequestHttpException("Image: %s not found".formatted(imageId));
        }

        return new FileSystemResource(file);
    }

    @Override
    public ImageInfo loadImageInfo(UUID imageId) {
        try {
            return objectMapper.readValue(findImageInfoById(imageId).toFile(), ImageInfo.class);
        } catch (IOException e) {
            throw new InternalErrorHttpException("Failed to load image-info: %s".formatted(imageId) + e);
        }
    }

    @Override
    public void delete(UUID imageId) {
        try {
            var imageInfoFile = findImageInfoById(imageId);
            
            if (imageInfoFile == null) {
                throw new BadRequestHttpException("Image: %s not found".formatted(imageId));
            }

            // Удаляем все файлы изображения (во всех форматах)
            try (Stream<Path> paths = Files.list(storageLocation)) {
                paths.filter(path -> {
                    String fileName = path.getFileName().toString();
                    return fileName.startsWith(imageId.toString()) && !fileName.endsWith(".json");
                }).forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted image file: {}", path);
                    } catch (IOException e) {
                        log.error("Failed to delete image file: {}", path, e);
                    }
                });
            }

            // Удаляем метаданные
            Files.delete(imageInfoFile);
        } catch (IOException e) {
            throw new InternalErrorHttpException("Failed to delete image: %s".formatted(imageId), e);
        }
    }

    private Path findImageById(UUID imageId) {
        try (Stream<Path> paths = Files.list(storageLocation)) {
            return paths
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith(imageId.toString()) && 
                               !fileName.endsWith(".json");
                    })
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new InternalErrorHttpException("Failed to load image: %s".formatted(imageId), e);
        }
    }

    private Path findImageInfoById(UUID imageId) {
        try (Stream<Path> paths = Files.list(storageLocation)) {
            return paths
                    .filter(path -> path.getFileName().startsWith(imageId + ".json"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new InternalErrorHttpException("Failed to load image-info: %s".formatted(imageId), e);
        }
    }
}
