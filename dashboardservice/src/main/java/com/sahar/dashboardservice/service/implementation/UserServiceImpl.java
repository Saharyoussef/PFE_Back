package com.sahar.dashboardservice.service.implementation;

import com.sahar.dashboardservice.domain.Response;
import com.sahar.dashboardservice.exception.ApiException;
import com.sahar.dashboardservice.handler.RestClientInterceptor;
import com.sahar.dashboardservice.model.User;
import com.sahar.dashboardservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static com.sahar.dashboardservice.utils.RequestUtils.convertResponse;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final RestClient restClient;

    public UserServiceImpl() {
        this.restClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .baseUrl("http://localhost:8085")
                //.defaultUriVariables(Map.of("some variable", "some value"))
                //.defaultHeader("AUTHORIZATION", "Bearer some value")
                .requestInterceptor(new RestClientInterceptor())
                //.requestInitializer(null)
                .build();
    }

    @Override
    public User getUserByUuid(String userUuid) {
        try {
            var response = restClient.get().uri("/user/profile").retrieve().body(Response.class);
            return convertResponse(response, User.class, "user");
        } catch (AccessDeniedException exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

}