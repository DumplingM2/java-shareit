package ru.practicum.shareit.server.booking.mapper;

import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.server.booking.Booking;

public interface BookingMapper {

    BookingDto mapToDto(Booking booking);

    Booking mapToBooking(NewBookingDto newBookingDto);
}
