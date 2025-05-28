package ru.practicum.shareit.gateway.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

@ExtendWith(MockitoExtension.class)
class GatewayErrorAttributesTest {

    private static final String ERROR_ATTRIBUTE =
            "org.springframework.boot.web.reactive.error" + ".DefaultErrorAttributes.ERROR";
    private GatewayErrorAttributes gatewayErrorAttributes;
    @Mock
    private ServerRequest mockRequest;
    @Mock
    private RequestPath mockRequestPath;
    @Mock
    private ServerWebExchange mockServerWebExchange;
    @Mock
    private ServerHttpRequest mockServerHttpRequest;

    @BeforeEach
    void setUp() {
        gatewayErrorAttributes = new GatewayErrorAttributes();

        when(mockRequest.exchange()).thenReturn(mockServerWebExchange);
        when(mockServerWebExchange.getRequest()).thenReturn(mockServerHttpRequest);

        when(mockRequest.requestPath()).thenReturn(mockRequestPath);
    }

    @Test
    @DisplayName("getErrorAttributes should return custom status and message when error is "
            + "ResponseStatusException")
    void getErrorAttributes_whenErrorIsResponseStatusException_shouldReturnCustomStatusAndMessage() {
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        String expectedReason = "Invalid request parameter";
        ResponseStatusException rse = new ResponseStatusException(expectedStatus, expectedReason);

        when(mockRequest.attribute(ERROR_ATTRIBUTE)).thenReturn(Optional.of(rse));

        Map<String, Object> errorAttributes = gatewayErrorAttributes.getErrorAttributes(mockRequest,
                ErrorAttributeOptions.defaults());

        assertThat("Error attributes should contain the correct status code", errorAttributes,
                hasEntry("status", expectedStatus.value()));
        assertThat("Error attributes should contain the correct message (reason)", errorAttributes,
                hasEntry("error", expectedReason));
        assertThat(
                "Error attributes should not contain 'exception' key for ResponseStatusException",
                errorAttributes, not(hasKey("exception")));
        assertThat("Error attributes should not contain 'trace' key", errorAttributes,
                not(hasKey("trace")));
    }

    @Test
    @DisplayName("getErrorAttributes should return default message when error is "
            + "ResponseStatusException with null reason")
    void getErrorAttributes_whenErrorIsResponseStatusExceptionWithNullReason_shouldReturnDefaultMessage() {
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND;
        ResponseStatusException rse = new ResponseStatusException(expectedStatus, null);

        when(mockRequest.attribute(ERROR_ATTRIBUTE)).thenReturn(Optional.of(rse));

        Map<String, Object> errorAttributes = gatewayErrorAttributes.getErrorAttributes(mockRequest,
                ErrorAttributeOptions.defaults());

        assertThat("Error attributes should contain the correct status code", errorAttributes,
                hasEntry("status", expectedStatus.value()));
        assertThat("Error attributes should contain the default message when reason is null",
                errorAttributes, hasEntry("error", "Invalid request"));
        assertThat(
                "Error attributes should not contain 'exception' key for ResponseStatusException",
                errorAttributes, not(hasKey("exception")));
        assertThat("Error attributes should not contain 'trace' key", errorAttributes,
                not(hasKey("trace")));
    }

    @Test
    @DisplayName("getErrorAttributes should return INTERNAL_SERVER_ERROR status and message when "
            + "error is another Throwable")
    void getErrorAttributes_whenErrorIsOtherThrowable_shouldReturnInternalServerErrorStatusAndMessage() {
        String errorMessage = "A wild error appeared!";
        Throwable otherError = new RuntimeException(errorMessage);

        when(mockRequest.attribute(ERROR_ATTRIBUTE)).thenReturn(Optional.of(otherError));

        Map<String, Object> errorAttributes = gatewayErrorAttributes.getErrorAttributes(mockRequest,
                ErrorAttributeOptions.defaults());

        assertThat("Error attributes should contain INTERNAL_SERVER_ERROR status", errorAttributes,
                hasEntry("status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        assertThat("Error attributes should contain the error message", errorAttributes,
                hasEntry("error", errorMessage));
        assertThat("Error attributes should contain 'exception' key for other Throwables",
                errorAttributes, hasEntry("exception", otherError.getClass().getName()));
        assertThat("Error attributes should not contain 'trace' key", errorAttributes,
                not(hasKey("trace")));
    }

    @Test
    @DisplayName("getErrorAttributes should return default message when error is another "
            + "Throwable with no message")
    void getErrorAttributes_whenErrorIsOtherThrowableWithNoMessage_shouldReturnDefaultMessage() {
        Throwable otherErrorWithNullMessage = new Throwable();

        when(mockRequest.attribute(ERROR_ATTRIBUTE)).thenReturn(
                Optional.of(otherErrorWithNullMessage));

        Map<String, Object> errorAttributes = gatewayErrorAttributes.getErrorAttributes(mockRequest,
                ErrorAttributeOptions.defaults());

        assertThat("Error attributes should contain INTERNAL_SERVER_ERROR status", errorAttributes,
                hasEntry("status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        assertThat("Error attributes should contain the default message when error message is null",
                errorAttributes, hasEntry("error", "Internal gateway error"));
        assertThat("Error attributes should contain 'exception' key for other Throwables",
                errorAttributes, hasEntry("exception", otherErrorWithNullMessage.getClass().getName()));
        assertThat("Error attributes should not contain 'trace' key", errorAttributes,
                not(hasKey("trace")));
    }
}