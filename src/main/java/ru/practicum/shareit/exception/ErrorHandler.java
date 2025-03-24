package ru.practicum.shareit.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    // Ошибки валидации @Valid (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage(), e);
        return new ErrorResponse("Validation error: " + e.getMessage());
    }

    // Ошибка, когда не найден пользователь, вещь и т.п.
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        log.error("Not found error: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    // Конфликты при создании/обновлении (например, email уже существует)
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException e) {
        log.error("Conflict error: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    // Общая ошибка валидации данных без @Valid
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomValidationException(ValidationException e) {
        log.error("Validation error: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    // Отсутствует заголовок X-Sharer-User-Id
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
        log.error("Missing header: {}", e.getHeaderName(), e);
        return new ErrorResponse("Missing header: " + e.getHeaderName());
    }

    // Если вдруг прилетела любая другая неперехваченная ошибка
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return new ErrorResponse("Unexpected error: " + e.getMessage());
    }
}
