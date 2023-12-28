package com.jaman.service;

import reactor.core.publisher.Mono;
import com.jaman.model.GraphQlRequestConfig;

public interface GraphQlOAuthService {
    <T> Mono<T> retrieve(GraphQlRequestConfig graphQlRequestConfig, Class<T> responseType);
}
