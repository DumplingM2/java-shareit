package ru.practicum.shareit.common.dto.booking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

@DisplayName("NewBookingDto Validation Tests")
class NewBookingDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("testValidNewBookingDto should have no violations for a valid DTO")
    void testValidNewBookingDto() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        NewBookingDto newBookingDto = new NewBookingDto(1L, start, end);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat("Should have no validation violations for a valid DTO", violations, is(empty()));
    }

    @Test
    @DisplayName("testNewBookingDtoWithNullItemId should have violation for null itemId")
    void testNewBookingDtoWithNullItemId() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        NewBookingDto newBookingDto = new NewBookingDto(null, start, end);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat("Should have exactly one violation for null itemId", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Item ID cannot be null"))),
                        hasProperty("propertyPath", hasToString("itemId"))
                )
        ));
    }

    @Test
    @DisplayName("testNewBookingDtoWithNullStart should have violation for null start date")
    void testNewBookingDtoWithNullStart() {
        NewBookingDto newBookingDto = new NewBookingDto(1L, null, LocalDateTime.now().plusHours(2));

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat("Should have exactly one violation for null start date", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Start date cannot be null"))),
                        hasProperty("propertyPath", hasToString("start"))
                )
        ));
    }

    @Test
    @DisplayName("testNewBookingDtoWithNullEnd should have violation for null end date")
    void testNewBookingDtoWithNullEnd() {
        NewBookingDto newBookingDto = new NewBookingDto(1L, LocalDateTime.now().plusHours(1), null);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat("Should have exactly one violation for null end date", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("End date cannot be null"))),
                        hasProperty("propertyPath", hasToString("end"))
                )
        ));
    }

    @Test
    @DisplayName("testNewBookingDtoWithAllNulls should have violations for all null fields")
    void testNewBookingDtoWithAllNulls() {
        NewBookingDto newBookingDto = new NewBookingDto(null, null, null);

        Set<ConstraintViolation<NewBookingDto>> violations = validator.validate(newBookingDto);

        assertThat("Should have violations for itemId, start, and end when all are null", violations, containsInAnyOrder(
                allOf(hasProperty("message", is(equalTo("Item ID cannot be null"))), hasProperty("propertyPath", hasToString("itemId"))),
                allOf(hasProperty("message", is(equalTo("Start date cannot be null"))), hasProperty("propertyPath", hasToString("start"))),
                allOf(hasProperty("message", is(equalTo("End date cannot be null"))), hasProperty("propertyPath", hasToString("end")))
        ));
    }
}