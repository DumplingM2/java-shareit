package ru.practicum.shareit.gateway.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class QueryParamValidationFilter {

    public <E extends Enum<E>> GatewayFilter validateOptionalEnumQueryParam(
            String paramName, Class<E> enumClass) {

        Set<String> allowedValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());

        return (exchange, chain) -> {
            List<String> paramValues = exchange.getRequest().getQueryParams().get(paramName);

            if (CollectionUtils.isEmpty(paramValues) || !StringUtils.hasText(
                    paramValues.getFirst())) {
                log.trace(
                        "Optional query parameter '{}' is not present or empty, allowing request.",
                        paramName);
                return chain.filter(exchange);
            }

            String actualValue = paramValues.getFirst().toUpperCase();

            if (allowedValues.contains(actualValue)) {
                log.trace("Optional query parameter '{}' has valid value '{}', allowing request.",
                        paramName, actualValue);
                return chain.filter(exchange);
            } else {
                log.warn(
                        "Validation failed: Query parameter '{}' has invalid value '{}'. Allowed "
                                + "values are: {}",
                        paramName, paramValues.getFirst(), allowedValues);
                String errorMessage = String.format("Unknown %s: %s", paramName,
                        paramValues.getFirst());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
            }
        };
    }
}