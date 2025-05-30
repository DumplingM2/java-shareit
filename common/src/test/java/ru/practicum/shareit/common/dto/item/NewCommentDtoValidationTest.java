package ru.practicum.shareit.common.dto.item;

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

@DisplayName("NewCommentDto Validation Tests")
class NewCommentDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("testValidNewCommentDto should have no violations for valid text")
    void testValidNewCommentDto() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("This is a valid comment.");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat("Should find no validation violations for valid comment text", violations,
                is(empty()));
    }

    @Test
    @DisplayName("testNewCommentDtoWithBlankText should have violation for blank text")
    void testNewCommentDtoWithBlankText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat("Should find a violation for blank comment text", violations, contains(
                allOf(hasProperty("message", is(equalTo("Comment text cannot be blank"))),
                        hasProperty("propertyPath", hasToString("text")))));
    }

    @Test
    @DisplayName("testNewCommentDtoWithNullText should have violation for null text")
    void testNewCommentDtoWithNullText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText(null);

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat("Should find a violation for null comment text", violations, contains(
                allOf(hasProperty("message", is(equalTo("Comment text cannot be blank"))),
                        hasProperty("propertyPath", hasToString("text")))));
    }

    @Test
    @DisplayName("testNewCommentDtoWithWhitespaceText should have violation for whitespace text")
    void testNewCommentDtoWithWhitespaceText() {
        NewCommentDto newCommentDto = new NewCommentDto();
        newCommentDto.setText("   ");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(newCommentDto);

        assertThat("Should find a violation for whitespace comment text", violations, contains(
                allOf(hasProperty("message", is(equalTo("Comment text cannot be blank"))),
                        hasProperty("propertyPath", hasToString("text")))));
    }
}