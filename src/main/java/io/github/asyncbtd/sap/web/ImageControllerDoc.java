package io.github.asyncbtd.sap.web;

import io.github.asyncbtd.sap.web.dto.ImageInfoResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

public interface ImageControllerDoc {

    ResponseEntity<ImageInfoResponse> uploadImage(
            MultipartFile file,
            String description
    );

    ResponseEntity<StreamingResponseBody> getImageById(
            UUID uuid
    );

    ResponseEntity<ImageInfoResponse> getImageInfoById(
            UUID uuid
    );

    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<?> deleteImage(
            UUID uuid
    );
}
