package io.github.asyncbtd.sap.config;

import io.github.asyncbtd.sap.config.prop.OpenSearchProps;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class OpenSearchConfig {

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchProps props) {
        HttpHost host = new HttpHost(props.getScheme(), props.getHost(), props.getPort());
        ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);

        if (StringUtils.hasText(props.getUsername()) && StringUtils.hasText(props.getPassword())) {
            // Создаем Basic Auth заголовок
            String credentials = props.getUsername() + ":" + props.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + encodedCredentials;
            
            // Настраиваем заголовок аутентификации через HTTP-клиент
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultHeaders(java.util.Arrays.asList(
                    new BasicHeader("Authorization", authHeader)
                ))
            );
        }

        return new OpenSearchClient(builder.build());
    }
}
