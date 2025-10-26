package io.github.asyncbtd.sap.config.prop;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("sap.image-storage")
public class ImageStorageProps {
    /// Path to save images on disk
    @NotNull
    private String path;
    /// Max size file
    private DataSize maxSize = DataSize.ofMegabytes(15L);
}
