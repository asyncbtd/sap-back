package io.github.asyncbtd.sap.core.storage;

import io.github.asyncbtd.sap.core.model.ImageInfo;
import io.github.asyncbtd.sap.web.dto.ImageInfoResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageStorage {
    void save(ImageInfo imageInfo, MultipartFile file);
    Resource load(UUID imageId);
    ImageInfo loadImageInfo(UUID imageId);
    void delete(UUID imageId);
}
