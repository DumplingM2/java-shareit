package ru.practicum.shareit.common.dto.booking;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewBookingDto {
    @NotNull(message = "Item ID cannot be null")
    private Long itemId;
    @NotNull(message = "Start date cannot be null")
    private LocalDateTime start;
    @NotNull(message = "End date cannot be null")
    private LocalDateTime end;
}
