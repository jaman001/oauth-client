package com.jaman.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import com.jaman.cache.OauthCacheManager;
import com.jaman.exception.ServiceException;
import com.jaman.model.RestRequestConfig;
import com.jaman.service.RestOauthService;

import java.time.Duration;

@Slf4j
public class RestOauthServiceImpl implements RestOauthService {
    private final WebClient client;
    private final String baseUrl;
    private final String name;
    private final String type;
    private final String subscriptionKey;
    private OauthCacheManager oauthCacheManager;

    public RestOauthServiceImpl(String baseUrl, String subscriptionKey, String name, String type,
                                OauthCacheManager oauthCacheManager) {
        this.client = WebClient.builder().baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
        this.subscriptionKey = subscriptionKey;
        this.name = name;
        this.type = type;
        this.oauthCacheManager = oauthCacheManager;
    }

    @Override
    public <T> Mono<T> makeRestCall(RestRequestConfig restRequestConfig, Class<T> responseType) {
        return getOAuthToken().flatMap(token -> makeRestCallInternal(restRequestConfig, responseType, token))
                              .onErrorResume(error -> {
                                  log.error("Error occurred", error);
                                  if (error instanceof WebClientResponseException.Unauthorized) {
                                      //Token expired/invalid; Retry.
                                      oauthCacheManager.remove(name + type);
                                      return getOAuthToken().flatMap(token -> {
                                          return makeRestCallInternal(restRequestConfig, responseType, token);
                                      });
                                  }
                                  return Mono.error(error);
                              });
    }

    public <T> Mono<T> makeRestCallInternal(RestRequestConfig restRequestConfig, Class<T> responseType, String token) {
        String url = baseUrl + restRequestConfig.getUri();
        WebClient.RequestHeadersSpec<?> requestSpec;
        HttpHeaders headers = new HttpHeaders();
        if (restRequestConfig.getHeaders() != null) {
            headers.addAll(restRequestConfig.getHeaders());
        }
        headers.add("Subscription-Key", subscriptionKey);
        headers.add("Content-Type", "application/json");
        if (token != null) {
            log.info("Oauth Token is present, using it {}", token);
            headers.add("Authorization", "Bearer " + token);
        }
        if (restRequestConfig.getRequestBody() != null) {
            requestSpec = client.method(restRequestConfig.getMethod()).uri(url).headers(httpHeaders -> {
                httpHeaders.addAll(headers);
            }).bodyValue(restRequestConfig.getRequestBody());
        } else {
            requestSpec = client.method(restRequestConfig.getMethod()).uri(url).headers(httpHeaders -> {
                httpHeaders.addAll(headers);
            });
        }
        return requestSpec.retrieve()
                          .onStatus(HttpStatusCode::is5xxServerError,
                           response -> Mono.error(new ServiceException("Server error", response.statusCode().value())))
                          .bodyToMono(responseType)
                          .retryWhen(Retry.backoff(2, Duration.ofSeconds(5))
                                          .filter(throwable -> throwable instanceof ServiceException)
                                          .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                              throw new ServiceException("External Service failed to process after max retries",
                                               HttpStatus.SERVICE_UNAVAILABLE.value());
                                          }));
    }

    public Mono<String> getOAuthToken() {
        log.info("Checking if token is available in cache");
        return oauthCacheManager.get(name + type, fetchOauthToken());
    }

    public Mono<String> fetchOauthToken() {
        log.info("Going to request fresh token");
        RestRequestConfig restRequestConfig = RestRequestConfig.builder().method(HttpMethod.POST).uri("/oauth/token").build();
        return makeRestCallInternal(restRequestConfig, String.class, null);
    }

}
