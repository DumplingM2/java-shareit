package ru.practicum.shareit.item.dto; // Assuming it belongs here

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemStatus;

@Data
@AllArgsConstructor
public class ItemWithBookingInfoDto {
    private Long id;
    private String name;
    private String description;
    private ItemStatus status;

    private Set<CommentDto> comments;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    @JsonProperty("available")
    @SuppressWarnings("unused")
    public boolean isAvailable() {
        return status == ItemStatus.AVAILABLE;
    }
}