
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.jaman.model.GraphQlRequestConfig;
import com.jaman.model.RestRequestConfig;
import com.jaman.service.GraphQlOAuthService;
import com.jaman.service.RestOauthService;

import java.util.Map;

@RestController
@Slf4j
public class BestController {

    @Autowired
    private Map<String, RestOauthService> restServiceClients;

    @Autowired
    private Map<String, GraphQlOAuthService> graphQlServiceClients;

    @RequestMapping(value = "/rest", method = RequestMethod.GET)
    public Mono<String> testRestApimEndpoint() {
        HttpHeaders customHeaders = new HttpHeaders();
        customHeaders.add("customHeader", "headervalue");
        // Add custom headers if needed
        RestPostRequest restPostRequest = RestPostRequest.builder().somevalue("value").build();
        RestRequestConfig request = RestRequestConfig.builder()
                                                     .method(HttpMethod.POST)
                                                     .uri("/your-rest-endpoint")
                                                     .headers(customHeaders)
                                                     .requestBody(smsRequest)
                                                     .build();
        // Request body (can be any object, or null for GET)
        return apimRestServiceClients.get("weather-external").makeRestCall(request, String.class).onErrorResume(error -> {
            log.error("Error occurred ..", error);
            //handle your errors
            return Mono.error(new PACSException());
        });
    }

    @RequestMapping(value = "/graphql", method = RequestMethod.GET)
    public Mono<Object> testGraphQlApimService() {
        GraphQlRequestConfig graphQlRequest = GraphQlRequestConfig.builder()
                                                                  .retrieve("graphDocument")
                                                                  .documentName("documentName")
                                                                  .variable("variable")
                                                                  .variableValue("1234AB")
                                                                  .build();
        return apimGraphQlServiceClients.get("graph-external")
                                        .retrieve(graphQlRequest, ResponseDTO.class)
                                        .onErrorResume(error -> {
                                            log.error("Error occurred ..", error);
                                            //handle your errors
                                            return Mono.error(new PACSException());
                                        });
    }
}
