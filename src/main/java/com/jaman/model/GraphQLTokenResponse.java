package com.jaman.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("ext_expires_in")
    private String extExpiresIn;

    @JsonProperty("expires_on")
    private String expiresOn;

    @JsonProperty("not_before")
    private String notBefore;

    private String resource;

    @JsonProperty("access_token")
    private String accessToken;
}
