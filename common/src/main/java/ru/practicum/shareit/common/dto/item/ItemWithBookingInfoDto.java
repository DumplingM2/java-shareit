package ru.practicum.shareit.common.dto.item;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.common.dto.booking.BookingShortDto;

@Data
@AllArgsConstructor
public class ItemWithBookingInfoDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    private Set<CommentDto> comments;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
}