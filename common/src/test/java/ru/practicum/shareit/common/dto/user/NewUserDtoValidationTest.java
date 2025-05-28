package ru.practicum.shareit.common.dto.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NewUserDto Validation Tests")
class NewUserDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Valid DTO should have no violations")
    void validDto_shouldHaveNoViolations() {
        NewUserDto dto = new NewUserDto("John Doe", "john.doe@example.com");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Valid NewUserDto should result in no validation violations", violations,
                is(empty()));
    }

    @Test
    @DisplayName("Blank name should cause violation")
    void blankName_shouldCauseViolation() {
        NewUserDto dto = new NewUserDto("", "john.doe@example.com");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly one violation for a blank name", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Name cannot be blank"))),
                        hasProperty("propertyPath", hasToString("name")),
                        hasProperty("invalidValue", is(equalTo("")))
                )
        ));
    }

    @Test
    @DisplayName("Null name should cause violation")
    void nullName_shouldCauseViolation() {
        NewUserDto dto = new NewUserDto(null, "john.doe@example.com");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly one violation for a null name", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Name cannot be blank"))),
                        hasProperty("propertyPath", hasToString("name")),
                        hasProperty("invalidValue", is(nullValue()))
                )
        ));
    }

    @Test
    @DisplayName("Whitespace name should cause violation")
    void whitespaceName_shouldCauseViolation() {
        NewUserDto dto = new NewUserDto("   ", "john.doe@example.com");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly one violation for a whitespace name", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Name cannot be blank"))),
                        hasProperty("propertyPath", hasToString("name")),
                        hasProperty("invalidValue", is(equalTo("   ")))
                )
        ));
    }

    @Test
    @DisplayName("Blank email should cause @NotBlank violation")
    void blankEmail_shouldCauseNotBlankViolation() {
        NewUserDto dto = new NewUserDto("John Doe", "");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);
        assertThat("Should find one violation for a blank email (@NotBlank)", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Email cannot be blank"))),
                        hasProperty("propertyPath", hasToString("email")),
                        hasProperty("invalidValue", is(equalTo("")))
                )
        ));
    }

    @Test
    @DisplayName("Null email should cause @NotBlank violation")
    void nullEmail_shouldCauseNotBlankViolation() {
        NewUserDto dto = new NewUserDto("John Doe", null);

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find one violation for a null email (@NotBlank)", violations, contains(
                allOf(
                        hasProperty("message", is(equalTo("Email cannot be blank"))),
                        hasProperty("propertyPath", hasToString("email")),
                        hasProperty("invalidValue", is(nullValue()))
                )
        ));
    }

    @Test
    @DisplayName("Whitespace email should cause @NotBlank violation")
    void whitespaceEmail_shouldCauseNotBlankViolation() {
        NewUserDto dto = new NewUserDto("John Doe", "   ");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find at least one violation for whitespace email", violations,
                hasSize(greaterThanOrEqualTo(1)));
        assertThat("Should find violation for whitespace email (@NotBlank or @Email)", violations,
                hasItem(
                        allOf(
                                hasProperty("message", is(oneOf("Email cannot be blank", "Invalid email format"))),
                                hasProperty("propertyPath", hasToString("email")),
                                hasProperty("invalidValue", is(equalTo("   ")))
                        )
                ));
    }

    @Test
    @DisplayName("Invalid email format (no @) should cause @Email violation")
    void invalidEmailFormat_noAtSign_shouldCauseEmailViolation() {
        String invalidEmail = "johndoeexample.com";
        NewUserDto dto = new NewUserDto("John Doe", invalidEmail);

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly one violation for invalid email format (no @)", violations,
                contains(
                        allOf(
                                hasProperty("message", is(equalTo("Invalid email format"))),
                                hasProperty("propertyPath", hasToString("email")),
                                hasProperty("invalidValue", is(equalTo(invalidEmail)))
                        )
                ));
    }

    @Test
    @DisplayName("Invalid email format (no domain) should cause @Email violation")
    void invalidEmailFormat_noDomain_shouldCauseEmailViolation() {
        String invalidEmail = "johndoe@";
        NewUserDto dto = new NewUserDto("John Doe", invalidEmail);

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly one violation for invalid email format (no domain)",
                violations, contains(
                        allOf(
                                hasProperty("message", is(equalTo("Invalid email format"))),
                                hasProperty("propertyPath", hasToString("email")),
                                hasProperty("invalidValue", is(equalTo(invalidEmail)))
                        )
                ));
    }

    @Test
    @DisplayName("Invalid email format (no user part) should cause @Email violation")
    void invalidEmailFormat_noUserPart_shouldCauseEmailViolation() {
        String invalidEmail = "@example.com";
        NewUserDto dto = new NewUserDto("John Doe", invalidEmail);

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly one violation for invalid email format (no user part)",
                violations, contains(
                        allOf(
                                hasProperty("message", is(equalTo("Invalid email format"))),
                                hasProperty("propertyPath", hasToString("email")),
                                hasProperty("invalidValue", is(equalTo(invalidEmail)))
                        )
                ));
    }

    @Test
    @DisplayName("Null name and null email should cause two violations")
    void nullNameAndNullEmail_shouldCauseTwoViolations() {
        NewUserDto dto = new NewUserDto(null, null);

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly two violations for null name and null email", violations,
                hasSize(2));
        assertThat("Violations should include null name and null email", violations,
                containsInAnyOrder(
                        allOf(
                                hasProperty("message", is(equalTo("Name cannot be blank"))),
                                hasProperty("propertyPath", hasToString("name")),
                                hasProperty("invalidValue", is(nullValue()))
                        ),
                        allOf(
                                hasProperty("message", is(equalTo("Email cannot be blank"))),
                                hasProperty("propertyPath", hasToString("email")),
                                hasProperty("invalidValue", is(nullValue()))
                        )
                ));
    }

    @Test
    @DisplayName("Blank name and invalid email format should cause two violations")
    void blankNameAndInvalidEmailFormat_shouldCauseTwoViolations() {
        String invalidEmail = "nodomain@";
        NewUserDto dto = new NewUserDto(" ", invalidEmail);

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(dto);

        assertThat("Should find exactly two violations for blank name and invalid email format",
                violations, hasSize(2));
        assertThat("Violations should include blank name and invalid email format", violations,
                containsInAnyOrder(
                        allOf(
                                hasProperty("message", is(equalTo("Name cannot be blank"))),
                                hasProperty("propertyPath", hasToString("name")),
                                hasProperty("invalidValue", is(equalTo(" ")))
                        ),
                        allOf(
                                hasProperty("message", is(equalTo("Invalid email format"))),
                                hasProperty("propertyPath", hasToString("email")),
                                hasProperty("invalidValue", is(equalTo(invalidEmail)))
                        )
                ));
    }
}