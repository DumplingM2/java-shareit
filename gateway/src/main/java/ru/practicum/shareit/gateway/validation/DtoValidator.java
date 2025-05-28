package ru.practicum.shareit.gateway.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DtoValidator {

    private final Validator validator;

    public <T> void validate(T dto) {
        if (dto == null) {
            log.warn("DTO validation failed: Input object is null");
            throw new ServerWebInputException("Request body is missing or invalid");
        }
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .filter(Objects::nonNull)
                    .findFirst().orElse("Unknown validation error");
            log.warn("Validation failed for DTO [{}]: {}", dto.getClass().getSimpleName(),
                    errorMessages);
            throw new ServerWebInputException("Validation failed: " + errorMessages);
        }
        log.debug("Validation successful for DTO: {}", dto);
    }
}