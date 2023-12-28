package com.jaman.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GraphQlRequestConfig {
    private String documentName;
    private String variable;
    private String variableValue;
    private String retrieve;
}
