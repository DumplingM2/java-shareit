package ru.practicum.shareit.server.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

@JsonTest
@DisplayName("ItemRequestDto JSON Serialization Tests")
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    @DisplayName("should serialize with items list")
    void testSerialize_WithItems() throws IOException {
        Long requestId = 1L;
        String description = "Need a saw";
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        ItemShortDto item1 = new ItemShortDto(10L, "Saw A", "Hand saw", true, 2L, requestId);
        ItemShortDto item2 = new ItemShortDto(11L, "Saw B", "Power saw", true, 3L, requestId);

        ItemRequestDto dto = new ItemRequestDto(requestId, description, created, Set.of(item1, item2));

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(requestId.intValue());
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.format(formatter));

        assertThat(result).hasJsonPath("$.items");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(2);
        assertThat(result).hasJsonPath("$.items[?(@.id == 10)]");
        assertThat(result).hasJsonPath("$.items[?(@.id == 11)]");
        assertThat(result).extractingJsonPathArrayValue("$.items[?(@.id == 10)].name").containsExactly("Saw A");
        assertThat(result).extractingJsonPathArrayValue("$.items[?(@.id == 11)].ownerId").containsExactly(3);
    }

    @Test
    @DisplayName("should serialize with empty items list")
    void testSerialize_EmptyItems() throws IOException {
        Long requestId = 2L;
        String description = "Need nothing specific";
        LocalDateTime created = LocalDateTime.now();

        ItemRequestDto dto = new ItemRequestDto(requestId, description, created, Collections.emptySet());

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(requestId.intValue());
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.format(formatter));

        assertThat(result).hasJsonPath("$.items");
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    @DisplayName("should serialize with null items list")
    void testSerialize_NullItems() throws IOException {
        Long requestId = 3L;
        String description = "Requesting something";
        LocalDateTime created = LocalDateTime.now().minusHours(5);

        ItemRequestDto dto = new ItemRequestDto(requestId, description, created, null);

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(requestId.intValue());
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.format(formatter));

        assertThat(result).hasJsonPath("$.items");
        assertThat(result).extractingJsonPathValue("$.items").isNull();
    }

    @Test
    @DisplayName("should serialize with null description and created")
    void testSerialize_NullDescAndCreated() throws IOException {
        Long requestId = 4L;
        ItemShortDto item = new ItemShortDto(15L, "Item C", "Desc C", false, 4L, requestId);

        ItemRequestDto dto = new ItemRequestDto(requestId, null, null, Set.of(item));

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(requestId.intValue());
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isNull();
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").isNull();
        assertThat(result).hasJsonPath("$.items");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(15);
    }
}