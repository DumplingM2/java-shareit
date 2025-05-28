package ru.practicum.shareit.server.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;

@JsonTest
@DisplayName("UpdateUserDto JSON Deserialization Tests")
class UpdateUserDtoJsonTest {

    @Autowired
    private JacksonTester<UpdateUserDto> json;

    @Test
    @DisplayName("should deserialize with all fields")
    void testDeserialize_AllFields() throws IOException {
        String jsonContent = "{\"name\":\"Updated Name\",\"email\":\"updated@example.com\"}";

        UpdateUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Updated Name");
        assertThat(resultDto.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("should deserialize with only name")
    void testDeserialize_NameOnly() throws IOException {
        String jsonContent = "{\"name\":\"Updated Name\"}";

        UpdateUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Updated Name");
        assertThat(resultDto.getEmail()).isNull();
    }

    @Test
    @DisplayName("should deserialize with only email")
    void testDeserialize_EmailOnly() throws IOException {
        String jsonContent = "{\"email\":\"updated@example.com\"}";

        UpdateUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("should deserialize with null values")
    void testDeserialize_NullValues() throws IOException {
        String jsonContent = "{\"name\":null,\"email\":null}";

        UpdateUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getEmail()).isNull();
    }

    @Test
    @DisplayName("should deserialize with extra fields ignored")
    void testDeserialize_ExtraFields() throws IOException {
        String jsonContent = "{\"name\":\"Updated Name\",\"email\":\"updated@example.com\",\"id\":1}";

        UpdateUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Updated Name");
        assertThat(resultDto.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("should deserialize empty JSON object")
    void testDeserialize_EmptyObject() throws IOException {
        String jsonContent = "{}";

        UpdateUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getEmail()).isNull();
    }
}