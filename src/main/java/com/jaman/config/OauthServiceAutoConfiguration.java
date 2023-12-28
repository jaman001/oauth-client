package com.jaman.config;

import com.jaman.service.GraphQlOAuthService;
import com.jaman.service.RestOauthService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(OauthServiceProperties.class)
public class OauthServiceAutoConfiguration {
    @Bean
    public Map<String, RestOauthService> apimRestServiceClients(OauthServiceProperties properties,
                                                                OauthServiceClientFactory oauthServiceClientFactory) {
        List<OauthServiceConfig> configs = properties.getRest();
        return oauthServiceClientFactory.createRestClients(configs);
    }

    @Bean
    public Map<String, GraphQlOAuthService> apimGraphQlServiceClients(OauthServiceProperties properties,
                                                                      OauthServiceClientFactory oauthServiceClientFactory) {
        List<OauthServiceConfig> configs = properties.getGraphql();
        return oauthServiceClientFactory.createGraphQlClients(configs);
    }
}