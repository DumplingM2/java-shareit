package ru.practicum.shareit.server.dto.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonTest
@DisplayName("NewBookingDto JSON Deserialization Tests")
class NewBookingDtoJsonTest {

    @Autowired
    private JacksonTester<NewBookingDto> json;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    @DisplayName("should deserialize with all fields present")
    void testDeserialize_AllFields() throws IOException {
        Long itemId = 10L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        String jsonContent = String.format(
                "{\"itemId\":%d,\"start\":\"%s\",\"end\":\"%s\"}",
                itemId, start.format(formatter), end.format(formatter)
        );

        NewBookingDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getItemId()).isEqualTo(itemId);
        assertThat(resultDto.getStart()).isEqualTo(start);
        assertThat(resultDto.getEnd()).isEqualTo(end);
    }

    @Test
    @DisplayName("should deserialize with null itemId")
    void testDeserialize_NullItemId() throws IOException {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(4);
        String jsonContent = String.format(
                "{\"itemId\":null,\"start\":\"%s\",\"end\":\"%s\"}",
                start.format(formatter), end.format(formatter)
        );

        NewBookingDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getItemId()).isNull();
        assertThat(resultDto.getStart()).isEqualTo(start);
        assertThat(resultDto.getEnd()).isEqualTo(end);
    }

    @Test
    @DisplayName("should deserialize with null start date")
    void testDeserialize_NullStartDate() throws IOException {
        Long itemId = 15L;
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        String jsonContent = String.format(
                "{\"itemId\":%d,\"start\":null,\"end\":\"%s\"}",
                itemId, end.format(formatter)
        );

        NewBookingDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getItemId()).isEqualTo(itemId);
        assertThat(resultDto.getStart()).isNull();
        assertThat(resultDto.getEnd()).isEqualTo(end);
    }

    @Test
    @DisplayName("should deserialize with null end date")
    void testDeserialize_NullEndDate() throws IOException {
        Long itemId = 20L;
        LocalDateTime start = LocalDateTime.now().plusMinutes(30);
        String jsonContent = String.format(
                "{\"itemId\":%d,\"start\":\"%s\",\"end\":null}",
                itemId, start.format(formatter)
        );

        NewBookingDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getItemId()).isEqualTo(itemId);
        assertThat(resultDto.getStart()).isEqualTo(start);
        assertThat(resultDto.getEnd()).isNull();
    }


    @Test
    @DisplayName("should deserialize ignoring extra fields")
    void testDeserialize_ExtraFields() throws IOException {
        Long itemId = 25L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        String jsonContent = String.format(
                "{\"itemId\":%d,\"start\":\"%s\",\"end\":\"%s\", \"bookerId\": 123}",
                itemId, start.format(formatter), end.format(formatter)
        );

        NewBookingDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getItemId()).isEqualTo(itemId);
        assertThat(resultDto.getStart()).isEqualTo(start);
        assertThat(resultDto.getEnd()).isEqualTo(end);
    }

    @Test
    @DisplayName("should deserialize empty JSON object")
    void testDeserialize_EmptyObject() throws IOException {
        String jsonContent = "{}";
        NewBookingDto resultDto = json.parse(jsonContent).getObject();

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getItemId()).isNull();
        assertThat(resultDto.getStart()).isNull();
        assertThat(resultDto.getEnd()).isNull();
    }
}