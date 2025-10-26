package io.github.asyncbtd.sap.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("sap.openapi")
public class OpenApiProps {
    private String url;
    private String version;
}
