package com.jaman.config;

import lombok.Data;

@Data
public class OauthServiceConfig {
    private String name;
    private String type;
    private String baseUrl;
    private String subscriptionKey;
}
