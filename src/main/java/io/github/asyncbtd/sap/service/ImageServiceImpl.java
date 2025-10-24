package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.core.service.ImageService;
import io.github.asyncbtd.sap.core.storage.ImageStorage;
import io.github.asyncbtd.sap.web.dto.ImageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageStorage imageStorage;

    public UUID saveImage(MultipartFile file) {
        validateImage(file);
        UUID imageId = UUID.randomUUID();
        imageStorage.save(imageId, file);
        return imageId;
    }

    public Resource loadImage(UUID imageId) {
        return imageStorage.load(imageId);
    }

    public void deleteImage(UUID imageId) {
        imageStorage.delete(imageId);
    }

    public List<ImageInfo> getAllImages() {
        return imageStorage.listAll();
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("The file is not an image");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("The file size exceeds 10MB");
        }
    }
}
