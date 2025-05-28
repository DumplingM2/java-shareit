package ru.practicum.shareit.server.dto.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;

@JsonTest
@DisplayName("UpdateItemDto JSON Deserialization Tests")
class UpdateItemDtoJsonTest {

    @Autowired
    private JacksonTester<UpdateItemDto> json;

    @Test
    @DisplayName("should deserialize with all fields")
    void testDeserialize_AllFields() throws IOException {
        String jsonContent = "{\"name\":\"Updated Name\",\"description\":\"New desc\",\"available\":false}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Updated Name");
        assertThat(resultDto.getDescription()).isEqualTo("New desc");
        assertThat(resultDto.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("should deserialize with only name")
    void testDeserialize_NameOnly() throws IOException {
        String jsonContent = "{\"name\":\"Updated Name\"}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Updated Name");
        assertThat(resultDto.getDescription()).isNull();
        assertThat(resultDto.getAvailable()).isNull();
    }

    @Test
    @DisplayName("should deserialize with only description")
    void testDeserialize_DescriptionOnly() throws IOException {
        String jsonContent = "{\"description\":\"New description\"}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getDescription()).isEqualTo("New description");
        assertThat(resultDto.getAvailable()).isNull();
    }

    @Test
    @DisplayName("should deserialize with only available")
    void testDeserialize_AvailableOnly() throws IOException {
        String jsonContent = "{\"available\":true}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getDescription()).isNull();
        assertThat(resultDto.getAvailable()).isTrue();
    }

    @Test
    @DisplayName("should deserialize with null values")
    void testDeserialize_NullValues() throws IOException {
        String jsonContent = "{\"name\":null,\"description\":null,\"available\":null}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getDescription()).isNull();
        assertThat(resultDto.getAvailable()).isNull();
    }

    @Test
    @DisplayName("should deserialize ignoring extra fields")
    void testDeserialize_ExtraFields() throws IOException {
        String jsonContent = "{\"name\":\"Updated\",\"description\":null,\"available\":false,\"id\":10}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Updated");
        assertThat(resultDto.getDescription()).isNull();
        assertThat(resultDto.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("should deserialize empty JSON object")
    void testDeserialize_EmptyObject() throws IOException {
        String jsonContent = "{}";
        UpdateItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getDescription()).isNull();
        assertThat(resultDto.getAvailable()).isNull();
    }
}