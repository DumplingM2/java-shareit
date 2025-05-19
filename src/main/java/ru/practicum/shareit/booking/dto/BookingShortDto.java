package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingShortDto {
    private Long id;
    private Long bookerId;
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}