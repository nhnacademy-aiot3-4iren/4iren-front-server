package com.nhnacademy.front.config;

import com.nhnacademy.front.properties.ApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class RestClientConfig {

    @Bean("gatewayRestClient")
    public RestClient gatewayRestClient(ApiProperties apiProperties) {
        return RestClient.builder()
                .baseUrl(apiProperties.getGatewayUrl())
                .build();
    }
}
