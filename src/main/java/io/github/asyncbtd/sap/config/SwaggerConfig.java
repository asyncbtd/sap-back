package io.github.asyncbtd.sap.config;

import io.github.asyncbtd.sap.config.prop.OpenApiProps;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private final OpenApiProps props;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(info())
                .servers(servers())
                .components(components());
    }

    private Info info() {
        return new Info()
                .title("Searching for Additional Problems API")
                .version(props.getVersion());
    }

    private List<Server> servers() {
        return List.of(new Server()
                .url(props.getUrl())
        );
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);
    }
}
