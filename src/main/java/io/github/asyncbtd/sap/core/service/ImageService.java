package io.github.asyncbtd.sap.core.service;

import io.github.asyncbtd.sap.web.dto.ImageInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    UUID saveImage(MultipartFile file);
    Resource loadImage(UUID imageId);
    void deleteImage(UUID imageId);
    List<ImageInfo> getAllImages();
}
