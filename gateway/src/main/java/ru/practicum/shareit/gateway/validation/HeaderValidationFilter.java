package ru.practicum.shareit.gateway.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class HeaderValidationFilter {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    public GatewayFilter validateUserIdHeader() {
        return (exchange, chain) -> {
            log.trace("Applying validation for header: {}", USER_ID_HEADER);
            String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);

            if (userId == null || userId.isBlank()) {
                log.warn("Validation failed: Header '{}' is missing or blank", USER_ID_HEADER);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Required header '" + USER_ID_HEADER + "' is missing");
            }

            try {
                Long.parseLong(userId);
                log.trace("Header '{}' is present and is a number: {}", USER_ID_HEADER, userId);
            } catch (NumberFormatException e) {
                log.warn("Validation failed: Header '{}' is not a valid number: {}", USER_ID_HEADER,
                        userId, e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid format for header '" + USER_ID_HEADER + "'");
            }

            return chain.filter(exchange);
        };
    }
}