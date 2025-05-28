package ru.practicum.shareit.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.gateway.validation.DtoValidator;

@Configuration
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class UserRoutesConfig {

    private final DtoValidator dtoValidator;

    @Value("${shareit-server.url}")
    private String serverUri;

    private static final String USERS_PATH = "/users";
    private static final String USERS_ID_PATH = USERS_PATH + "/{id}";

    @Bean
    public RouteLocator userRoutes(RouteLocatorBuilder builder) {
        log.info("Configuring routes for User service at URI: {}", serverUri);

        return builder.routes()
                // Route: POST /users -> Create User
                .route("create_user", r -> r
                        .path(USERS_PATH)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f.modifyRequestBody(
                                NewUserDto.class,
                                NewUserDto.class,
                                (exchange, dto) -> {
                                    log.debug("Validating NewUserDto for POST {}", USERS_PATH);
                                    dtoValidator.validate(dto);
                                    return Mono.just(dto);
                                }
                        ))
                        .uri(serverUri))

                // Route: PATCH /users/{id} -> Update User
                .route("update_user", r -> r
                        .path(USERS_ID_PATH)
                        .and()
                        .method(HttpMethod.PATCH)
                        .filters(f -> f.modifyRequestBody(
                                UpdateUserDto.class,
                                UpdateUserDto.class,
                                (exchange, dto) -> {
                                    String userId = exchange.getRequest().getURI().getPath()
                                            .substring(USERS_PATH.length() + 1);
                                    log.debug("Validating UpdateUserDto for PATCH /users/{}", userId);
                                    dtoValidator.validate(dto);
                                    return Mono.just(dto);
                                }
                        ))
                        .uri(serverUri))

                // Route: GET /users -> Get All Users
                .route("get_all_users", r -> r
                        .path(USERS_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .uri(serverUri))

                // Route: GET /users/{id} -> Get User By ID
                .route("get_user_by_id", r -> r
                        .path(USERS_ID_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .uri(serverUri))

                // Route: DELETE /users/{id} -> Delete User By ID
                .route("delete_user", r -> r
                        .path(USERS_ID_PATH)
                        .and()
                        .method(HttpMethod.DELETE)
                        .uri(serverUri))

                .build();
    }
}