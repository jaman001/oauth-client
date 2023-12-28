package com.jaman.service;

import reactor.core.publisher.Mono;
import com.jaman.model.RestRequestConfig;

public interface RestOauthService {
    <T> Mono<T> makeRestCall(RestRequestConfig restRequestConfig, Class<T> responseType);
}
