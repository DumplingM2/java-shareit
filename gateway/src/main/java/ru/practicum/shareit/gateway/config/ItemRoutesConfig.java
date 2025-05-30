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
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.gateway.validation.DtoValidator;
import ru.practicum.shareit.gateway.validation.HeaderValidationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemRoutesConfig {

    private final DtoValidator dtoValidator;
    private final HeaderValidationFilter headerValidationFilter;

    @Value("${shareit-server.url}")
    private String serverUri;

    private static final String ITEMS_PATH = "/items";
    private static final String ITEMS_ID_PATH = ITEMS_PATH + "/{id}";
    private static final String ITEMS_SEARCH_PATH = ITEMS_PATH + "/search";
    private static final String ITEMS_COMMENT_PATH = ITEMS_PATH + "/{itemId}/comment";

    @Bean
    public RouteLocator itemRoutes(RouteLocatorBuilder builder) {
        log.info("Configuring routes for Item service at URI: {}", serverUri);

        return builder.routes()
                // Route: POST /items -> Create Item
                .route("create_item", r -> r
                        .path(ITEMS_PATH)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .modifyRequestBody(
                                        NewItemDto.class, NewItemDto.class,
                                        (exchange, dto) -> {
                                            log.debug("Validating NewItemDto for POST {}", ITEMS_PATH);
                                            dtoValidator.validate(dto);
                                            return Mono.just(dto);
                                        }
                                ))
                        .uri(serverUri))

                // Route: PATCH /items/{id} -> Update Item
                .route("update_item", r -> r
                        .path(ITEMS_ID_PATH)
                        .and()
                        .method(HttpMethod.PATCH)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .modifyRequestBody(
                                        UpdateItemDto.class, UpdateItemDto.class,
                                        (exchange, dto) -> {
                                            String itemId = exchange.getRequest().getURI().getPath()
                                                    .substring(ITEMS_PATH.length() + 1);
                                            log.debug("Validating UpdateItemDto for PATCH /items/{}", itemId);
                                            dtoValidator.validate(dto);
                                            return Mono.just(dto);
                                        }
                                ))
                        .uri(serverUri))

                // Route: GET /items -> Get User's Items
                .route("get_user_items", r -> r
                        .path(ITEMS_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                // Route: GET /items/{id} -> Get Item By ID
                .route("get_item_by_id", r -> r
                        .path(ITEMS_ID_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                // Route: GET /items/search -> Search Items
                .route("search_items", r -> r
                        .path(ITEMS_SEARCH_PATH)
                        .and()
                        .method(HttpMethod.GET)
                        .and()
                        .query("text")
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                // Route: DELETE /items?id={id} -> Delete Item By ID
                .route("delete_item", r -> r
                        .path(ITEMS_ID_PATH)
                        .and()
                        .method(HttpMethod.DELETE)
                        .filters(f -> f.filter(headerValidationFilter.validateUserIdHeader()))
                        .uri(serverUri))

                // Route: POST /{itemId}/comment -> Add Comment
                .route("add_comment", r -> r
                        .path(ITEMS_COMMENT_PATH)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .filter(headerValidationFilter.validateUserIdHeader())
                                .modifyRequestBody(
                                        NewCommentDto.class, NewCommentDto.class,
                                        (exchange, dto) -> {
                                            String path = exchange.getRequest().getURI().getPath();
                                            String itemId = path.substring(ITEMS_PATH.length() + 1,
                                                    path.indexOf("/comment"));
                                            log.debug("Validating NewCommentDto for POST /items/{}/comment",
                                                    itemId);
                                            dtoValidator.validate(dto);
                                            return Mono.just(dto);
                                        }
                                ))
                        .uri(serverUri))

                .build();
    }
}