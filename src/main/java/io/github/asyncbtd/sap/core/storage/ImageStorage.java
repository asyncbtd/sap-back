package io.github.asyncbtd.sap.core.storage;

import io.github.asyncbtd.sap.web.dto.ImageInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageStorage {
    void save(UUID imageId, MultipartFile file);
    Resource load(UUID imageId);
    void delete(UUID imageId);
    List<ImageInfo> listAll();
}
