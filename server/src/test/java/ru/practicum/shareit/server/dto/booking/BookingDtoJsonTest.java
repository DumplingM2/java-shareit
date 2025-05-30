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
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.user.UserDto;
import ru.practicum.shareit.common.enums.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonTest
@DisplayName("BookingDto JSON Serialization Tests")
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    @DisplayName("should serialize with all fields populated")
    void testSerialize_AllFields() throws IOException {
        Long bookingId = 100L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        ItemDto itemDto = new ItemDto(10L, "Item Name", "Item Desc", true);
        UserDto bookerDto = new UserDto(2L, "Booker Name", "booker@mail.com");
        String status = BookingStatus.WAITING.toString();

        BookingDto dto = new BookingDto(bookingId, itemDto, bookerDto, start, end, status);

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(bookingId.intValue());
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.format(formatter));
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.format(formatter));
        assertThat(result).hasJsonPath("$.status");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(status);

        assertThat(result).hasJsonPath("$.item");
        assertThat(result).hasJsonPath("$.item.id");
        assertThat(result).hasJsonPath("$.item.name");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(itemDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(itemDto.getAvailable());

        assertThat(result).hasJsonPath("$.booker");
        assertThat(result).hasJsonPath("$.booker.id");
        assertThat(result).hasJsonPath("$.booker.name");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(bookerDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo(bookerDto.getName());
    }

    @Test
    @DisplayName("should serialize with null nested DTOs")
    void testSerialize_NullNestedDtos() throws IOException {
        Long bookingId = 101L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(5);
        String status = BookingStatus.APPROVED.toString();

        BookingDto dto = new BookingDto(bookingId, null, null, start, end, status);

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(bookingId.intValue());
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.format(formatter));
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.format(formatter));
        assertThat(result).hasJsonPath("$.status");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(status);

        assertThat(result).hasJsonPath("$.item");
        assertThat(result).extractingJsonPathValue("$.item").isNull();
        assertThat(result).hasJsonPath("$.booker");
        assertThat(result).extractingJsonPathValue("$.booker").isNull();
    }

    @Test
    @DisplayName("should serialize with null dates and status")
    void testSerialize_NullDatesAndStatus() throws IOException {
        Long bookingId = 102L;
        ItemDto itemDto = new ItemDto(12L, "Another Item", "Desc", true);
        UserDto bookerDto = new UserDto(3L, "Another Booker", "b2@mail.com");

        BookingDto dto = new BookingDto(bookingId, itemDto, bookerDto, null, null, null);

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(bookingId.intValue());
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").isNull();
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").isNull();
        assertThat(result).hasJsonPath("$.status");
        assertThat(result).extractingJsonPathStringValue("$.status").isNull();

        assertThat(result).hasJsonPath("$.item.id");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(itemDto.getId().intValue());
        assertThat(result).hasJsonPath("$.booker.id");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(bookerDto.getId().intValue());
    }
}