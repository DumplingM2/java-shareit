package ru.practicum.shareit.server.dto.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.common.dto.item.NewItemDto;

@JsonTest
@DisplayName("NewItemDto JSON Deserialization Tests")
class NewItemDtoJsonTest {

    @Autowired
    private JacksonTester<NewItemDto> json;

    @Test
    @DisplayName("should deserialize with all fields including requestId")
    void testDeserialize_AllFieldsWithRequestId() throws IOException {
        String jsonContent = "{\"name\":\"Ladder\",\"description\":\"Tall ladder\",\"available\":true,\"requestId\":5}";

        NewItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Ladder");
        assertThat(resultDto.getDescription()).isEqualTo("Tall ladder");
        assertThat(resultDto.getAvailable()).isTrue();
        assertThat(resultDto.getRequestId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("should deserialize with requestId missing")
    void testDeserialize_RequestIdMissing() throws IOException {
        String jsonContent = "{\"name\":\"Wrench\",\"description\":\"Adjustable wrench\",\"available\":false}";

        NewItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Wrench");
        assertThat(resultDto.getDescription()).isEqualTo("Adjustable wrench");
        assertThat(resultDto.getAvailable()).isFalse();
        assertThat(resultDto.getRequestId()).isNull();
    }

    @Test
    @DisplayName("should deserialize with requestId null")
    void testDeserialize_RequestIdNull() throws IOException {
        String jsonContent = "{\"name\":\"Screwdriver\",\"description\":\"Phillips head\",\"available\":true,\"requestId\":null}";

        NewItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Screwdriver");
        assertThat(resultDto.getDescription()).isEqualTo("Phillips head");
        assertThat(resultDto.getAvailable()).isTrue();
        assertThat(resultDto.getRequestId()).isNull();
    }

    @Test
    @DisplayName("should deserialize with other fields null")
    void testDeserialize_OtherFieldsNull() throws IOException {
        String jsonContent = "{\"name\":null,\"description\":null,\"available\":null,\"requestId\":20}";

        NewItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isNull();
        assertThat(resultDto.getDescription()).isNull();
        assertThat(resultDto.getAvailable()).isNull();
        assertThat(resultDto.getRequestId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("should deserialize ignoring extra fields")
    void testDeserialize_ExtraFields() throws IOException {
        String jsonContent = "{\"name\":\"Pliers\",\"description\":\"Needle nose\",\"available\":true,\"requestId\":7,\"extra\":\"ignored\"}";

        NewItemDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Pliers");
        assertThat(resultDto.getDescription()).isEqualTo("Needle nose");
        assertThat(resultDto.getAvailable()).isTrue();
        assertThat(resultDto.getRequestId()).isEqualTo(7L);
    }
}