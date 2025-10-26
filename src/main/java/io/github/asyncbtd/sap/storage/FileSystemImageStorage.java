package io.github.asyncbtd.sap.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.core.exception.BadRequestHttpException;
import io.github.asyncbtd.sap.core.exception.InternalErrorHttpException;
import io.github.asyncbtd.sap.core.model.ImageFormat;
import io.github.asyncbtd.sap.core.model.ImageInfo;
import io.github.asyncbtd.sap.core.storage.ImageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileSystemImageStorage implements ImageStorage {

    private final ObjectMapper objectMapper;
    private final Path storageLocation;

    public FileSystemImageStorage(ObjectMapper objectMapper, ImageStorageProps imageStorageProps) {
        this.objectMapper = objectMapper;
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
            var imageFormat = ImageFormat.fromMimeType(file.getContentType());
            var imagePath = storageLocation.resolve(imageInfo.id() + imageFormat.getExtension());

            if (imageFormat == ImageFormat.GIF) {
                Files.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                ImageIO.write(ImageIO.read(file.getInputStream()), "PNG", imagePath.toFile());
            }

            Path jsonPath = storageLocation.resolve(imageInfo.id() + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), imageInfo);

            log.info("Saved image and metadata: {}", imageInfo);
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
            var imageFile = findImageById(imageId);
            var imageInfoFile = findImageInfoById(imageId);

            if (imageFile == null || imageInfoFile == null) {
                throw new BadRequestHttpException("Image: %s not found".formatted(imageId));
            }

            Files.delete(imageFile);
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
                               (fileName.endsWith(".gif") || fileName.endsWith(".png"));
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

