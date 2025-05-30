package ru.practicum.shareit.gateway.exception;

import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

@Component
@SuppressWarnings("unused")
public class GatewayErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Throwable error = getError(request);

        String message;
        HttpStatus status;

        if (error instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : "Invalid request";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = error.getMessage() != null ? error.getMessage() : "Internal gateway error";
            errorAttributes.put("exception", error.getClass().getName());
        }

        errorAttributes.put("status", status.value());
        errorAttributes.put("error", message);
        errorAttributes.remove("trace");

        return errorAttributes;
    }
}