package io.github.asyncbtd.sap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.asyncbtd.sap.config.prop.ImageStorageProps;
import io.github.asyncbtd.sap.config.prop.KeycloakProps;
import io.github.asyncbtd.sap.config.prop.OpenApiProps;
import io.github.asyncbtd.sap.config.prop.OpenSearchProps;
import io.github.asyncbtd.sap.core.util.Validator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({
        KeycloakProps.class,
        ImageStorageProps.class,
        OpenApiProps.class,
        OpenSearchProps.class
})
@RequiredArgsConstructor
public class SapApplicationConfig {

    private final ImageStorageProps imageStorageProps;

    @Bean
    public RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

/*    @Bean
    public ImageFormatUtil imageFormatUtil(ImageStorageProps props) {
        return new ImageFormatUtil(props);
    }*/

    @PostConstruct
    public void validator() {
//        var validator = new Validator(props);
//        validator.setImageStorageProps(props);
//        return validator;
        Validator.init(imageStorageProps);
    }
}
