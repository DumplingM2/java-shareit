package ru.practicum.shareit.server.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.common.exception.ErrorMessage;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleNotFound should return 404 for NotFoundException")
    void handleNotFound_whenUserNotFoundException_shouldReturnNotFound() {
        String errorMessage = "User with id 99 not found";
        NotFoundException exception = new NotFoundException(errorMessage);
        int expectedStatus = 404;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleNotFound(exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleNotFound should return 404 for NotFoundException")
    void handleNotFound_whenItemNotFoundException_shouldReturnNotFound() {
        String errorMessage = "Item with id 99 not found";
        NotFoundException exception = new NotFoundException(errorMessage);
        int expectedStatus = 404;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleNotFound(exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleNotFound should return 404 for NotFoundException")
    void handleNotFound_whenBookingNotFoundException_shouldReturnNotFound() {
        String errorMessage = "Booking with id 99 not found";
        NotFoundException exception = new NotFoundException(errorMessage);
        int expectedStatus = 404;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleNotFound(exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleEmailAlreadyExists should return 409 for EmailAlreadyExistsException")
    void handleEmailAlreadyExists_whenEmailAlreadyExistsException_shouldReturnConflict() {
        String errorMessage = "Email test@example.com already exists";
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(errorMessage);
        int expectedStatus = 409;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleEmailAlreadyExists(
                exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.CONFLICT)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleAccessDenied should return 403 for AccessDeniedException")
    void handleAccessDenied_whenAccessDeniedException_shouldReturnForbidden() {
        String errorMessage = "User 2 does not own item 1";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);
        int expectedStatus = 403;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleAccessDenied(
                exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleMissingHeader should return 400 for BookingBadRequestException")
    void handleMissingHeader_whenBookingBadRequestException_shouldReturnBadRequest() {
        String errorMessage = "Item is not available";
        BookingBadRequestException exception = new BookingBadRequestException(errorMessage);
        int expectedStatus = 400;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleMissingHeader(
                exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleGenericException should return 500 for generic RuntimeException")
    void handleGenericException_whenGenericRuntimeException_shouldReturnInternalServerError() {
        String errorMessage = "An unexpected error occurred";
        RuntimeException exception = new RuntimeException(
                errorMessage);
        int expectedStatus = 500;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleGenericException(
                exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.INTERNAL_SERVER_ERROR)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }

    @Test
    @DisplayName("handleGenericException should return 500 for NullPointerException")
    void handleGenericException_whenNullPointerException_shouldReturnInternalServerError() {
        String errorMessage = "Null Pointer encountered";
        NullPointerException exception = new NullPointerException(
                errorMessage);
        int expectedStatus = 500;

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleGenericException(
                exception);

        assertNotNull(response);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.INTERNAL_SERVER_ERROR)));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getError(), is(equalTo(errorMessage)));
        assertThat(response.getBody().getResponseCode(), is(equalTo(expectedStatus)));
    }
}