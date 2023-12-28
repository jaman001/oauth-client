package com.jaman.config;

import com.jaman.service.GraphQlOAuthService;
import com.jaman.service.RestOauthService;
import com.jaman.service.impl.GraphQlOAuthServiceImpl;
import com.jaman.service.impl.RestOauthServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.jaman.cache.OauthCacheManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OauthServiceClientFactory {
    private final OauthCacheManager oauthCacheManager;

    public OauthServiceClientFactory(WebClient.Builder webClientBuilder, OauthCacheManager oauthCacheManager) {
        this.oauthCacheManager = oauthCacheManager;
    }

    public Map<String, RestOauthService> createRestClients(List<OauthServiceConfig> configs) {
        Map<String, RestOauthService> clients = new HashMap<>();
        for (OauthServiceConfig config : configs) {
            RestOauthService client =
             new RestOauthServiceImpl(config.getBaseUrl(), config.getSubscriptionKey(), config.getName(), config.getType(),
              oauthCacheManager);
            clients.put(config.getName(), client);
        }
        return clients;
    }

    public Map<String, GraphQlOAuthService> createGraphQlClients(List<OauthServiceConfig> configs) {
        Map<String, GraphQlOAuthService> clients = new HashMap<>();
        for (OauthServiceConfig config : configs) {
            GraphQlOAuthService client =
             new GraphQlOAuthServiceImpl(config.getBaseUrl(), config.getSubscriptionKey(), config.getName(), config.getType(),
              oauthCacheManager);
            clients.put(config.getName(), client);
        }
        return clients;
    }

}
