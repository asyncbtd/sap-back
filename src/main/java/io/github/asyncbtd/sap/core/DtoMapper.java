package io.github.asyncbtd.sap.core;

import io.github.asyncbtd.sap.core.model.ImageInfo;
import io.github.asyncbtd.sap.web.dto.ImageInfoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class DtoMapper {

    public ImageInfoResponse toDto(ImageInfo imageInfo) {
        if (imageInfo == null) {
            return null;
        }

        String uploadedByUser = null;
        if (imageInfo.uploadedByUser() != null) {
            uploadedByUser = imageInfo.uploadedByUser().username();
        }

        return ImageInfoResponse.builder()
                .id(imageInfo.id())
                .contentType(imageInfo.contentType())
                .size(imageInfo.size())
                .description(imageInfo.description())
                .uploadedByUser(uploadedByUser)
                .dateTimeUpload(imageInfo.dateTimeUpload())
                .build();
    }
}
