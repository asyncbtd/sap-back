package io.github.asyncbtd.sap.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("sap.opensearch")
public class OpenSearchProps {
    private String host = "localhost";
    private int port = 9200;
    private String scheme = "http";
    private String indexName = "images";
    private String username;
    private String password;
}
