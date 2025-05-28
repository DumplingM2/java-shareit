package ru.practicum.shareit.server.dto.item;

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
import ru.practicum.shareit.common.dto.booking.BookingShortDto;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

@JsonTest
@DisplayName("ItemWithBookingInfoDto JSON Serialization Tests")
class ItemWithBookingInfoDtoJsonTest {

    @Autowired
    private JacksonTester<ItemWithBookingInfoDto> json;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    private final Long itemId = 1L;
    private final String itemName = "Test Item";
    private final String itemDesc = "Description";
    private final Boolean itemAvailable = true;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Test
    @DisplayName("should serialize with all fields populated")
    void testSerialize_AllFields() throws IOException {
        LocalDateTime createdDate = LocalDateTime.now().minusDays(1);
        LocalDateTime lastBookingStart = LocalDateTime.now().minusDays(5);
        LocalDateTime lastBookingEnd = LocalDateTime.now().minusDays(4);
        LocalDateTime nextBookingStart = LocalDateTime.now().plusDays(1);
        LocalDateTime nextBookingEnd = LocalDateTime.now().plusDays(2);

        CommentDto comment = new CommentDto(10L, "Great!", itemId, "User A", createdDate.format(formatter));
        BookingShortDto lastBooking = new BookingShortDto(100L, 2L, itemId, lastBookingStart, lastBookingEnd);
        BookingShortDto nextBooking = new BookingShortDto(101L, 3L, itemId, nextBookingStart, nextBookingEnd);

        ItemWithBookingInfoDto dto = new ItemWithBookingInfoDto(
                itemId, itemName, itemDesc, itemAvailable, Set.of(comment), lastBooking, nextBooking
        );

        JsonContent<ItemWithBookingInfoDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemId.intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemName);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDesc);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemAvailable);

        assertThat(result).hasJsonPath("$.comments");
        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(1);
        assertThat(result).hasJsonPath("$.comments[0].id");
        assertThat(result).hasJsonPath("$.comments[0].text");
        assertThat(result).hasJsonPath("$.comments[0].authorName");
        assertThat(result).hasJsonPath("$.comments[0].created");
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Great!");

        assertThat(result).hasJsonPath("$.lastBooking");
        assertThat(result).hasJsonPath("$.lastBooking.id");
        assertThat(result).hasJsonPath("$.lastBooking.bookerId");
        assertThat(result).hasJsonPath("$.lastBooking.start");
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(100);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo(lastBookingStart.format(formatter));

        assertThat(result).hasJsonPath("$.nextBooking");
        assertThat(result).hasJsonPath("$.nextBooking.id");
        assertThat(result).hasJsonPath("$.nextBooking.bookerId");
        assertThat(result).hasJsonPath("$.nextBooking.end");
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(101);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end")
                .isEqualTo(nextBookingEnd.format(formatter));
    }

    @Test
    @DisplayName("should serialize with empty comments and null bookings")
    void testSerialize_EmptyAndNullNested() throws IOException {
        ItemWithBookingInfoDto dto = new ItemWithBookingInfoDto(
                itemId, itemName, itemDesc, itemAvailable,
                Collections.emptySet(),
                null,
                null
        );

        JsonContent<ItemWithBookingInfoDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemId.intValue());
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemName);

        assertThat(result).hasJsonPath("$.comments");
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();

        assertThat(result).hasJsonPath("$.lastBooking");
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).hasJsonPath("$.nextBooking");
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
    }

    @Test
    @DisplayName("should serialize with null comments set")
    void testSerialize_NullCommentsSet() throws IOException {
        ItemWithBookingInfoDto dto = new ItemWithBookingInfoDto(
                itemId, itemName, itemDesc, itemAvailable,
                null,
                null,
                null
        );

        JsonContent<ItemWithBookingInfoDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.comments");
        assertThat(result).extractingJsonPathValue("$.comments").isNull();

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemId.intValue());
    }
}