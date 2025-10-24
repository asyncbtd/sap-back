package io.github.asyncbtd.sap.storage;

import io.github.asyncbtd.sap.core.storage.ImageStorage;
import io.github.asyncbtd.sap.web.dto.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileSystemImageStorage implements ImageStorage {

    private final Path storageLocation;

    public FileSystemImageStorage(@Value("${sap.image-storage.location:./images}") String location) {
        this.storageLocation = Paths.get(location).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for storing images", e);
        }
    }

    @Override
    public void save(UUID imageId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            
            String filename = imageId.toString() + extension;
            Path targetLocation = this.storageLocation.resolve(filename);

            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    @Override
    public Resource load(UUID imageId) {
        try {
            Path file = findFileByUUID(imageId);
            
            if (file == null) {
                throw new RuntimeException("File not found: " + imageId);
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.info("Файл загружен: {}", file);
                return resource;
            } else {
                throw new RuntimeException("Failed to read file: " + imageId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public void delete(UUID imageId) {
        try {
            Path file = findFileByUUID(imageId);
            
            if (file != null) {
                Files.delete(file);
                log.info("File deleted: {}", file);
            } else {
                throw new RuntimeException("File not found: " + imageId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public List<ImageInfo> listAll() {
        List<ImageInfo> images = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(storageLocation, 1)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     String filename = path.getFileName().toString();
                     String uuidStr = filename.substring(0, filename.lastIndexOf('.'));
                     UUID uuid = UUID.fromString(uuidStr);

                     ImageInfo info = null;
                     try {
                         info = ImageInfo.builder()
                                 .id(uuid)
                                 .filename(filename)
                                 .size(Files.size(path))
                                 .contentType(Files.probeContentType(path))
                                 .uploadedAt(LocalDateTime.ofInstant(
                                         Files.getLastModifiedTime(path).toInstant(),
                                         ZoneId.systemDefault()))
                                 .build();
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }

                     images.add(info);
                 });
        } catch (IOException e) {
            throw new RuntimeException("Failed to get file list", e);
        }
        
        return images;
    }

    private Path findFileByUUID(UUID imageId) throws IOException {
        try (Stream<Path> paths = Files.list(storageLocation)) {
            return paths
                    .filter(path -> path.getFileName().toString().startsWith(imageId.toString()))
                    .findFirst()
                    .orElse(null);
        }
    }
}

