package ru.practicum.shareit.gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GatewayAbstractExceptionHandlerTest {

    @Mock
    private ErrorAttributes errorAttributes;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ServerCodecConfigurer serverCodecConfigurer;
    @Mock
    private ServerRequest serverRequest;

    private GatewayAbstractExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(applicationContext.getClassLoader()).thenReturn(this.getClass().getClassLoader());

        when(serverCodecConfigurer.getWriters()).thenReturn(Collections.emptyList());
        when(serverCodecConfigurer.getReaders()).thenReturn(Collections.emptyList());

        exceptionHandler = new GatewayAbstractExceptionHandler(errorAttributes, applicationContext,
                serverCodecConfigurer);
    }

    @Test
    @DisplayName("renderErrorResponse should return correct ServerResponse with status and "
            + "message from attributes")
    void renderErrorResponse_whenAttributesProvided_thenReturnsCorrectResponse() {
        int expectedStatus = HttpStatus.BAD_REQUEST.value();
        String expectedMessage = "Invalid request data";
        String requestPath = "/items";

        Map<String, Object> errorPropertiesMap = new HashMap<>();
        errorPropertiesMap.put("status", expectedStatus);
        errorPropertiesMap.put("message", expectedMessage);
        errorPropertiesMap.put("path", requestPath);

        when(errorAttributes.getErrorAttributes(eq(serverRequest),
                any(ErrorAttributeOptions.class))).thenReturn(errorPropertiesMap);

        Mono<ServerResponse> responseMono = ReflectionTestUtils.invokeMethod(exceptionHandler,
                "renderErrorResponse", serverRequest);

        assertNotNull(responseMono, "Mono<ServerResponse> should not be null");
        StepVerifier.create(responseMono).assertNext(response -> {
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode(),
                    "Response status code should be BAD_REQUEST");
            assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType(),
                    "Response content type should be APPLICATION_JSON");
        }).verifyComplete();
    }

    @Test
    @DisplayName("renderErrorResponse should use default status INTERNAL_SERVER_ERROR if 'status'"
            + " attribute is missing")
    void renderErrorResponse_whenStatusAttributeMissing_thenUsesDefaultStatusInternalServerError() {
        String expectedMessage = "Something went wrong internally";

        Map<String, Object> errorPropertiesMap = new HashMap<>();
        errorPropertiesMap.put("message", expectedMessage);
        errorPropertiesMap.put("path", "/users");

        when(errorAttributes.getErrorAttributes(eq(serverRequest),
                any(ErrorAttributeOptions.class))).thenReturn(errorPropertiesMap);

        Mono<ServerResponse> responseMono = ReflectionTestUtils.invokeMethod(exceptionHandler,
                "renderErrorResponse", serverRequest);

        assertNotNull(responseMono, "Mono<ServerResponse> should not be null");
        StepVerifier.create(responseMono).assertNext(response -> {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode(),
                    "Response status code should be INTERNAL_SERVER_ERROR when 'status' is missing");
            assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType(),
                    "Response content type should be APPLICATION_JSON");
        }).verifyComplete();
    }

    @Test
    @DisplayName("renderErrorResponse should use default message 'Unknown error' if 'message' "
            + "attribute is missing")
    void renderErrorResponse_whenMessageAttributeMissing_thenUsesDefaultMessage() {
        int expectedStatus = HttpStatus.NOT_FOUND.value();

        Map<String, Object> errorPropertiesMap = new HashMap<>();
        errorPropertiesMap.put("status", expectedStatus);
        errorPropertiesMap.put("path", "/bookings/99");

        when(errorAttributes.getErrorAttributes(eq(serverRequest),
                any(ErrorAttributeOptions.class))).thenReturn(errorPropertiesMap);

        Mono<ServerResponse> responseMono = ReflectionTestUtils.invokeMethod(exceptionHandler,
                "renderErrorResponse", serverRequest);

        assertNotNull(responseMono, "Mono<ServerResponse> should not be null");
        StepVerifier.create(responseMono).assertNext(response -> {
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode(),
                    "Response status code should match the provided status");
            assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType(),
                    "Response content type should be APPLICATION_JSON");
        }).verifyComplete();
    }

    @Test
    @DisplayName("renderErrorResponse should use default status and message if both attributes "
            + "are missing")
    void renderErrorResponse_whenBothAttributesMissing_thenUsesDefaults() {
        Map<String, Object> errorPropertiesMap = new HashMap<>();

        when(errorAttributes.getErrorAttributes(eq(serverRequest),
                any(ErrorAttributeOptions.class))).thenReturn(errorPropertiesMap);

        Mono<ServerResponse> responseMono = ReflectionTestUtils.invokeMethod(exceptionHandler,
                "renderErrorResponse", serverRequest);

        assertNotNull(responseMono, "Mono<ServerResponse> should not be null");
        StepVerifier.create(responseMono).assertNext(response -> {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode(),
                    "Response status code should default to INTERNAL_SERVER_ERROR");
            assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType(),
                    "Response content type should be APPLICATION_JSON");
        }).verifyComplete();
    }

    @Test
    @DisplayName("getRoutingFunction should return a RouterFunction routing all requests")
    void getRoutingFunction_shouldReturnRouterFunction() {

        var routerFunction = exceptionHandler.getRoutingFunction(errorAttributes);
        org.junit.jupiter.api.Assertions.assertNotNull(routerFunction,
                "RouterFunction should not be null");
    }
}