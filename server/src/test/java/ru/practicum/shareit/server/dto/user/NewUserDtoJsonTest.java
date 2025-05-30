package ru.practicum.shareit.server.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.common.dto.user.NewUserDto;

@JsonTest
@DisplayName("NewUserDto JSON Deserialization Tests")
class NewUserDtoJsonTest {

    @Autowired
    private JacksonTester<NewUserDto> json;

    @Test
    @DisplayName("should deserialize JSON to NewUserDto correctly")
    void testDeserialize() throws IOException {
        String jsonContent = "{\"name\":\"Jane Smith\",\"email\":\"jane.s@sample.org\"}";

        NewUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Jane Smith");
        assertThat(resultDto.getEmail()).isEqualTo("jane.s@sample.org");
    }

    @Test
    @DisplayName("should handle extra fields gracefully")
    void testDeserializeWithExtraField() throws IOException {
        String jsonContent = "{\"name\":\"Extra Field\",\"email\":\"extra@field.com\",\"id\":123}";

        NewUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Extra Field");
        assertThat(resultDto.getEmail()).isEqualTo("extra@field.com");
    }

    @Test
    @DisplayName("should handle null values in JSON")
    void testDeserializeWithNulls() throws IOException {
        String jsonContent = "{\"name\":null,\"email\":null}";

        NewUserDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getEmail()).isNull();
    }
}