package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.core.exception.BadRequestHttpException;
import io.github.asyncbtd.sap.core.model.ImageFormat;
import io.github.asyncbtd.sap.core.model.ImageInfo;
import io.github.asyncbtd.sap.core.service.ImageService;
import io.github.asyncbtd.sap.core.service.UserService;
import io.github.asyncbtd.sap.core.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageStorage imageStorage;
    private final ImageStorageProps imageStorageProps;
    private final UserService userService;

    @Override
    public ImageInfo saveImage(MultipartFile file, String description) {
        validateImage(file);
        var imageFormat = ImageFormat.fromMimeType(file.getContentType());
        var imageInfo = ImageInfo.builder()
                .id(UUID.randomUUID())
                .contentType(imageFormat.getContentType())
                .size(file.getSize())
                .description(description)
                .uploadedByUser(userService.getAuthorizedUser())
                .dateTimeUpload(LocalDateTime.now())
                .build();
        imageStorage.save(imageInfo, file);
        return imageInfo;
    }

    @Override
    public StreamingResponseBody getImage(UUID imageId) {
        return outputStream -> {
            try (var inputStream = imageStorage.load(imageId).getInputStream()) {
                outputStream.write(inputStream.readAllBytes());
            }
        };
    }

    @Override
    public ImageInfo getImageInfo(UUID imageId) {
        return imageStorage.loadImageInfo(imageId);
    }

    public void deleteImage(UUID imageId) {
        imageStorage.delete(imageId);
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestHttpException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestHttpException("The file is not an image");
        }

        var maxSize = imageStorageProps.getMaxSize();
        if (file.getSize() > maxSize.toBytes()) {
            throw new BadRequestHttpException("The file size exceeds " + maxSize.toMegabytes() + "MB");
        }
    }
}
