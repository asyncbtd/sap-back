package io.github.asyncbtd.sap.service;

import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.core.model.ImageInfo;
import io.github.asyncbtd.sap.core.service.ImageService;
import io.github.asyncbtd.sap.core.service.UserService;
import io.github.asyncbtd.sap.core.storage.ImageStorage;
import io.github.asyncbtd.sap.core.util.ImageFormatUtil;
import io.github.asyncbtd.sap.core.util.Validator;
import io.github.asyncbtd.sap.service.OpenSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageStorage imageStorage;
    private final UserService userService;
    private final ImageStorageProps imageStorageProps;
    private final OpenSearchService openSearchService;

    @Override
    public ImageInfo saveImage(MultipartFile file, String description) {
        // Валидация изображения через утилиту
        Validator.validateImage(file);

        String contentType = file.getContentType();
        List<String> availableFormats = ImageFormatUtil.getOutputFormats(contentType, imageStorageProps);

        var imageInfo = ImageInfo.builder()
                .id(UUID.randomUUID())
                .contentType(contentType)
                .size(file.getSize())
                .description(description)
                .uploadedByUser(userService.getAuthorizedUser())
                .dateTimeUpload(LocalDateTime.now())
                .availableFormats(availableFormats)
                .build();
        imageStorage.save(imageInfo, file);
        openSearchService.indexImage(imageInfo.id(), description);
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

    @Override
    public String getImageFileName(UUID imageId) {
        try {
            var resource = imageStorage.load(imageId);
            return resource.getFilename();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deleteImage(UUID imageId) {
        imageStorage.delete(imageId);
        openSearchService.deleteImage(imageId);
    }

    @Override
    public List<UUID> searchByDescription(String query) {
        return openSearchService.searchByDescription(query);
    }
}
