package ru.practicum.shareit.gateway.exception;

import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.common.exception.ErrorMessage;

@Component
@Order(-2)
@Slf4j
@SuppressWarnings("unused")
public class GatewayAbstractExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GatewayAbstractExceptionHandler(ErrorAttributes errorAttributes,
                                           ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    @NonNull
    protected RouterFunction<ServerResponse> getRoutingFunction(
            @NonNull ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @NonNull
    private Mono<ServerResponse> renderErrorResponse(@NonNull ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
                org.springframework.boot.web.error.ErrorAttributeOptions.defaults());

        int status = (int) Optional.ofNullable(errorPropertiesMap.get("status"))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String message = (String) Optional.ofNullable(errorPropertiesMap.get("error"))
                .orElse("Unknown error");

        ErrorMessage errorResponse = new ErrorMessage(message, status);

        log.error("Handling error: Status={}, Message='{}', Path={}", status, message,
                errorPropertiesMap.getOrDefault("path", "unknown"));

        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}