package io.github.asyncbtd.sap.web;

import io.github.asyncbtd.sap.core.DtoMapper;
import io.github.asyncbtd.sap.core.service.ImageService;
import io.github.asyncbtd.sap.web.dto.ImageInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController implements ImageControllerDoc {

    private final DtoMapper dtoMapper;
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageInfoResponse> uploadImage(
            @Valid @RequestParam("file") MultipartFile file,
            @Valid @RequestParam(value = "description", required = false) String description
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoMapper.toDto(imageService.saveImage(file, description)));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<StreamingResponseBody> getImageById(
            @Valid @PathVariable UUID uuid
    ) {
        var imageInfo = imageService.getImageInfo(uuid);
        var filename = imageService.getImageFileName(uuid);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(imageInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(imageService.getImage(uuid));
    }

    @GetMapping("/{uuid}/info")
    public ResponseEntity<ImageInfoResponse> getImageInfoById(
            @Valid @PathVariable UUID uuid
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(dtoMapper.toDto(imageService.getImageInfo(uuid)));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('IMAGE_DELETE')")
    public ResponseEntity<?> deleteImage(
            @Valid @PathVariable UUID uuid
    ) {
        imageService.deleteImage(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UUID>> searchImages(
            @Valid @RequestParam("q") String q
    ) {
        return ResponseEntity.ok(imageService.searchByDescription(q));
    }
}
