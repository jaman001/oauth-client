# OAUTH CLIENT UTILITY LIBRARY

___________________________________________________
Plug and play library to consume any apim rest/graphql service. 

### Features
1. Config based rest/graphql client creation
2. Access token management automated
3. Acess tokens cached locally with different expiry depending on internal/external service
4. Default retry mechanism (2 retries with 5 seconds wait)
___________________________________________________

## How to use

1.  Add this library as your maven dependency -
```
    <dependency>
    <groupId>com.jaman</groupId>
    <artifactId>oauth-client-util</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    </dependency>
```
2.  Add your apim services in app.yml like below (can refer sample app.yml in src/main/resources)-
```
apim:
  services:
    rest:
      - name: weather-external
        type: external
        base-url: https://weather.org/get
        subscription-key: XXXXXXXX
      - name: your-org-internal
        type: internal
        base-url: https://your.org/get
        subscription-key: XXXXXXXX
    graphql:
      - name: graphql-external
        type: external
        base-url: https://your-gql.com/retrieve
        subscription-key: XXXXXXXX
```
3.  Just autowire the clients' map which is automatically created for you -
```
    @Autowired
    private Map<String, ApimService> apimServiceClients;

    @Autowired
    private Map<String, ApimGraphQlService> apimGraphQlServiceClients;
```
4.  call your rest endpoint like below -
```
    // Create your request body if needed
    RestPostRequest restPostRequest = RestPostRequest.builder().somevalue("value").build();

    // Add custom headers if needed
    HttpHeaders customHeaders = new HttpHeaders();
    customHeaders.add("customHeader","headervalue");
    
    // Create request config object 
    RestRequestConfig request = RestRequestConfig.builder()
                                                 .method(HttpMethod.POST)
                                                 .uri("/your-rest-endpoint")
                                                 .headers(customHeaders)
                                                 .requestBody(restPostRequest)
                                                 .build(); 

    // Call your rest service like below              
    return apimServiceClients.get("weather-internal").makeRestCall(request, WeatherResponseDTO.class);
```
5.  Use your graphQl services like below -
```
    GraphQlRequestConfig graphQlRequest = GraphQlRequestConfig.builder()
                                                                  .retrieve("graphDocument")
                                                                  .documentName("documentName")
                                                                  .variable("variable")
                                                                  .variableValue("1234AB")
                                                                  .build();

    // Query your graphQl service like below
    return apimGraphQlServiceClients.get("graphql-internal").retrieve(graphQlRequest, ResponseDTO.class);
```
