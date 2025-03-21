package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ErrorHandler {

    // Ошибки валидации @Valid (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        return new ErrorResponse("Validation error: " + e.getMessage());
    }

    // Ошибка, когда не найден пользователь, вещь и т.п.
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    // Конфликты при создании/обновлении (например, email уже существует)
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException e) {
        return new ErrorResponse(e.getMessage());
    }

    // Общая ошибка валидации данных без @Valid
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomValidationException(ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    // Отсутствует заголовок X-Sharer-User-Id
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
        return new ErrorResponse("Missing header: " + e.getHeaderName());
    }

    // Если вдруг “прилетела” любая другая неперехваченная ошибка
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(Throwable e) {
        return new ErrorResponse("Unexpected error: " + e.getMessage());
    }
}
