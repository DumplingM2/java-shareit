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
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.gateway.validation.DtoValidator;
import ru.practicum.shareit.gateway.validation.HeaderValidationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemRequestRoutesConfig {

    private final DtoValidator dtoValidator;
    private final HeaderValidationFilter headerValidationFilter;

    @Value("${shareit-server.url}")
    private String serverUri;

    private static final String REQUESTS_PATH = "/requests";
    private static final String REQUESTS_ALL_PATH = REQUESTS_PATH + "/all";
    private static final String REQUESTS_ID_PATH = REQUESTS_PATH + "/{requestId}";

    @Bean
    public RouteLocator itemRequestRoutes(RouteLocatorBuilder builder) {
        log.info("Configuring routes for ItemRequest service at URI: {}", serverUri);

        return builder.routes()
                // Route: POST /requests -> Add new item request
                .route("create_request", r -> r
                        .path(REQUESTS_PATH)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .modifyRequestBody(
                                        NewItemRequestDto.class, NewItemRequestDto.class,
                                        (exchange, dto) -> {
                                            log.debug("Validating NewItemRequestDto for POST {}", REQUESTS_PATH);
                                            dtoValidator.validate(dto);
                                            return Mono.just(dto);
                                        }
                                ))
                        .uri(serverUri))

                // Route: GET /requests -> Get user's own requests
                .route("get_own_requests", r -> r
                        .path(REQUESTS_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                // Route: GET /requests/all -> Get requests from other users (paginated)
                .route("get_all_requests", r -> r
                        .path(REQUESTS_ALL_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                // Route: GET /requests/{requestId} -> Get a specific request by ID
                .route("get_request_by_id", r -> r
                        .path(REQUESTS_ID_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                .build();
    }
}