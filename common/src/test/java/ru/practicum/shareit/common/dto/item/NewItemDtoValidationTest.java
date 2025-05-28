package ru.practicum.shareit.common.dto.item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

@DisplayName("NewItemDto Validation Tests")
class NewItemDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("testValidNewItemDto should have no violations for a valid DTO")
    void testValidNewItemDto() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find no validation violations for a valid NewItemDto", violations,
                is(empty()));
    }

    @Test
    @DisplayName("testNewItemDtoWithBlankName should have violation for blank name")
    void testNewItemDtoWithBlankName() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for blank item name", violations, contains(
                allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                        hasProperty("propertyPath", hasToString("name")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithNullName should have violation for null name")
    void testNewItemDtoWithNullName() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(null);
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for null item name", violations, contains(
                allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                        hasProperty("propertyPath", hasToString("name")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithWhitespaceName should have violation for whitespace name")
    void testNewItemDtoWithWhitespaceName() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("   ");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for whitespace item name", violations, contains(
                allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                        hasProperty("propertyPath", hasToString("name")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithBlankDescription should have violation for blank description")
    void testNewItemDtoWithBlankDescription() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for blank item description", violations, contains(
                allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                        hasProperty("propertyPath", hasToString("description")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithNullDescription should have violation for null description")
    void testNewItemDtoWithNullDescription() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription(null);
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for null item description", violations, contains(
                allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                        hasProperty("propertyPath", hasToString("description")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithWhitespaceDescription should have violation for whitespace "
            + "description")
    void testNewItemDtoWithWhitespaceDescription() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("   ");
        newItemDto.setAvailable(true);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for whitespace item description", violations, contains(
                allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                        hasProperty("propertyPath", hasToString("description")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithNullAvailable should have violation for null available status")
    void testNewItemDtoWithNullAvailable() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName("Item Name");
        newItemDto.setDescription("Item Description");
        newItemDto.setAvailable(null);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat("Should find a violation for null item available status", violations, contains(
                allOf(hasProperty("message", is(equalTo("Item status must be set"))),
                        hasProperty("propertyPath", hasToString("available")))));
    }

    @Test
    @DisplayName("testNewItemDtoWithAllInvalidFields should have violations for all null/blank "
            + "fields")
    void testNewItemDtoWithAllInvalidFields() {
        NewItemDto newItemDto = new NewItemDto();
        newItemDto.setName(null);
        newItemDto.setDescription(null);
        newItemDto.setAvailable(null);

        Set<ConstraintViolation<NewItemDto>> violations = validator.validate(newItemDto);

        assertThat(
                "Should find violations for name, description, and available when all are invalid",
                violations, containsInAnyOrder(
                        allOf(hasProperty("message", is(equalTo("Name cannot be blank"))),
                                hasProperty("propertyPath", hasToString("name"))),
                        allOf(hasProperty("message", is(equalTo("Description cannot be blank"))),
                                hasProperty("propertyPath", hasToString("description"))),
                        allOf(hasProperty("message", is(equalTo("Item status must be set"))),
                                hasProperty("propertyPath", hasToString("available")))));
    }
}