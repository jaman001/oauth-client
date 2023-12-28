package com.jaman.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import com.jaman.cache.OauthCacheManager;
import com.jaman.model.GraphQLTokenResponse;
import com.jaman.model.GraphQlRequestConfig;
import com.jaman.model.RestRequestConfig;
import com.jaman.service.GraphQlOAuthService;

import java.time.Duration;

@Slf4j
public class GraphQlOAuthServiceImpl implements GraphQlOAuthService {
    private final HttpGraphQlClient graphQlClient;
    private final WebClient client;
    private final String baseUrl;
    private final String name;
    private final String type;
    private final String subscriptionKey;
    private OauthCacheManager oauthCacheManager;

    public GraphQlOAuthServiceImpl(String baseUrl, String subscriptionKey, String name, String type,
                                   OauthCacheManager oauthCacheManager) {
        this.client = WebClient.builder().baseUrl(baseUrl + "/gql/oauth/token").build();
        this.graphQlClient = HttpGraphQlClient.builder().url(baseUrl + "/api/graphql").build();
        this.baseUrl = baseUrl;
        this.subscriptionKey = subscriptionKey;
        this.name = name;
        this.type = type;
        this.oauthCacheManager = oauthCacheManager;
    }

    @Override
    public <T> Mono<T> retrieve(GraphQlRequestConfig graphQlRequestConfig, Class<T> responseType) {
        return getOAuthToken().flatMap(token -> retrieveInternal(graphQlRequestConfig, responseType, token))
                              .onErrorResume(error -> {
                                  log.error("Error occurred", error);
                                  if (error instanceof WebClientResponseException.Unauthorized) {
                                      //Token expired/invalid; Retry.
                                      oauthCacheManager.remove(name + type);
                                      return getOAuthToken().flatMap(token -> {
                                          return retrieveInternal(graphQlRequestConfig, responseType, token);
                                      });
                                  }
                                  return Mono.error(error);
                              });
    }

    public <T> Mono<T> retrieveInternal(GraphQlRequestConfig graphQlRequestConfig, Class<T> responseType, String token) {
        WebClient.RequestHeadersSpec<?> requestSpec;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Subscription-Key", subscriptionKey);
        log.info("Oauth Token is present, using it {}", token);
        headers.add("Authorization", "Bearer " + token);
        return graphQlClient.mutate()
                            .headers(httpHeaders -> {
                                httpHeaders.addAll(headers);
                            })
                            .build()
                            .documentName(graphQlRequestConfig.getDocumentName())
                            .variable(graphQlRequestConfig.getVariable(), graphQlRequestConfig.getVariableValue())
                            .retrieve(graphQlRequestConfig.getRetrieve())
                            .toEntity(responseType)
                            .retryWhen(Retry.backoff(2, Duration.ofSeconds(5)));
    }


    public <T> Mono<T> makeRestCallInternal(RestRequestConfig restRequestConfig, Class<T> responseType) {
        return client.method(restRequestConfig.getMethod())
                     .header("Content-Type", "application/json")
                     .header("Subscription-Key", subscriptionKey)
                     .retrieve()
                     .bodyToMono(responseType);
    }

    public Mono<String> getOAuthToken() {
        log.info("Checking if token is available in cache");
        return oauthCacheManager.get(name + type, fetchOauthToken());
    }

    public Mono<String> fetchOauthToken() {
        log.info("Going to request fresh token");
        RestRequestConfig restRequestConfig = RestRequestConfig.builder().method(HttpMethod.POST).build();
        return makeRestCallInternal(restRequestConfig, GraphQLTokenResponse.class).map(GraphQLTokenResponse::getAccessToken);
    }
}
