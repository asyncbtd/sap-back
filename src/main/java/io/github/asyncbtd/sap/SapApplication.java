package io.github.asyncbtd.sap;

import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.config.prop.KeycloakProps;
import io.github.asyncbtd.sap.config.prop.OpenApiProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        KeycloakProps.class,
        ImageStorageProps.class,
        OpenApiProps.class
})
public class SapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapApplication.class, args);
    }

}
