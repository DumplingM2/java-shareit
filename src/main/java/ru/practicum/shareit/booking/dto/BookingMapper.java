package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;

public interface BookingMapper {

    BookingDto mapToDto(Booking booking);

    Booking mapToBooking(NewBookingDto newBookingDto);
}