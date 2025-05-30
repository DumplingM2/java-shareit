package ru.practicum.shareit.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.exception.ErrorMessage;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    @ExceptionHandler({NotFoundException.class, NotFoundException.class,
            NotFoundException.class, ItemRequestNotFoundException.class})
    public ResponseEntity<ErrorMessage> handleNotFound(final RuntimeException e) {
        log.warn("Encountered {} while processing request: returning 404 Not Found",
                e.getClass().getSimpleName());
        return ResponseEntity.status(404).body(new ErrorMessage(e.getMessage(), 404));
    }

    @ExceptionHandler({EmailAlreadyExistsException.class})
    public ResponseEntity<ErrorMessage> handleEmailAlreadyExists(final RuntimeException e) {
        log.warn("Encountered {} while processing request: returning 409 Conflict",
                e.getClass().getSimpleName());
        return ResponseEntity.status(409).body(new ErrorMessage(e.getMessage(), 409));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDenied(final AccessDeniedException e) {
        log.warn("Encountered {} while processing request: returning 403 Forbidden",
                e.getClass().getSimpleName());
        return ResponseEntity.status(403).body(new ErrorMessage(e.getMessage(), 403));
    }

    @ExceptionHandler({MissingRequestHeaderException.class, BookingBadRequestException.class})
    public ResponseEntity<ErrorMessage> handleMissingHeader(final RuntimeException e) {
        log.warn("Encountered {} while processing request: returning 400 Bad Request",
                e.getClass().getSimpleName());
        return ResponseEntity.status(400).body(new ErrorMessage(e.getMessage(), 400));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessage> handleGenericException(final RuntimeException e) {
        log.warn("Encountered {} while processing request: returning 500 Internal Server Error",
                e.getClass().getSimpleName());
        return ResponseEntity.status(500).body(new ErrorMessage(e.getMessage(), 500));
    }

}
