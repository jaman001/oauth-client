package com.jaman.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "apim.services")
@Getter
@Setter
@Primary
public class OauthServiceProperties {
    private List<OauthServiceConfig> rest;
    private List<OauthServiceConfig> graphql;
}