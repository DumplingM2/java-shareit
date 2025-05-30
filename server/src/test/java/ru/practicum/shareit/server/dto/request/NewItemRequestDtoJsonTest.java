package ru.practicum.shareit.server.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;

import java.io.IOException;

@JsonTest
@DisplayName("NewItemRequestDto JSON Deserialization Tests")
class NewItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<NewItemRequestDto> json;

    @Test
    @DisplayName("should deserialize with description present")
    void testDeserialize_WithDescription() throws IOException {
        String description = "Looking for a size 10 wrench";
        String jsonContent = String.format("{\"description\":\"%s\"}", description);

        NewItemRequestDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("should deserialize with description null")
    void testDeserialize_NullDescription() throws IOException {
        String jsonContent = "{\"description\":null}";

        NewItemRequestDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getDescription()).isNull();
    }

    @Test
    @DisplayName("should deserialize with description missing")
    void testDeserialize_MissingDescription() throws IOException {
        String jsonContent = "{}";

        NewItemRequestDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getDescription()).isNull();
    }


    @Test
    @DisplayName("should deserialize ignoring extra fields")
    void testDeserialize_ExtraFields() throws IOException {
        String description = "Need paint roller";
        String jsonContent = String.format("{\"description\":\"%s\", \"requestId\": 123}", description);

        NewItemRequestDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getDescription()).isEqualTo(description);
    }
}