package io.github.asyncbtd.sap.core.service;

import io.github.asyncbtd.sap.core.model.ImageInfo;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

public interface ImageService {
    ImageInfo saveImage(MultipartFile file, String description);
    StreamingResponseBody getImage(UUID imageId);
    ImageInfo getImageInfo(UUID imageId);
    void deleteImage(UUID imageId);
}
