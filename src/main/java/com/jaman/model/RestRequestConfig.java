package com.jaman.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Builder
@Data
public class RestRequestConfig {
    private HttpMethod method;
    private String uri;
    private Object requestBody;
    private HttpHeaders headers;
}
