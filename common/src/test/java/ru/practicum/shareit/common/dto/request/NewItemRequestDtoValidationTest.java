package ru.practicum.shareit.common.dto.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NewItemRequestDto Validation Tests")
class NewItemRequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("testValidNewItemRequestDto should have no violations")
    void testValidNewItemRequestDto() {
        NewItemRequestDto dto = new NewItemRequestDto("Need a good drill for the weekend.");

        Set<ConstraintViolation<NewItemRequestDto>> violations = validator.validate(dto);

        assertThat("Valid DTO should have no violations", violations, is(empty()));
    }

    @Test
    @DisplayName("testDtoWithBlankDescription should have violation")
    void testDtoWithBlankDescription() {
        NewItemRequestDto dto = new NewItemRequestDto("");

        Set<ConstraintViolation<NewItemRequestDto>> violations = validator.validate(dto);

        assertThat("Should find one violation for blank description", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Request description cannot be blank"))),
                        hasProperty("propertyPath", hasToString("description"))
                )
        ));
    }

    @Test
    @DisplayName("testDtoWithNullDescription should have violation")
    void testDtoWithNullDescription() {
        NewItemRequestDto dto = new NewItemRequestDto(null);

        Set<ConstraintViolation<NewItemRequestDto>> violations = validator.validate(dto);

        assertThat("Should find one violation for null description", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Request description cannot be blank"))),
                        hasProperty("propertyPath", hasToString("description"))
                )
        ));
    }

    @Test
    @DisplayName("testDtoWithWhitespaceDescription should have violation")
    void testDtoWithWhitespaceDescription() {
        NewItemRequestDto dto = new NewItemRequestDto("   ");

        Set<ConstraintViolation<NewItemRequestDto>> violations = validator.validate(dto);

        assertThat("Should find one violation for whitespace description", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Request description cannot be blank"))),
                        hasProperty("propertyPath", hasToString("description"))
                )
        ));
    }
}