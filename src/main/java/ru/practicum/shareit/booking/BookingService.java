package ru.practicum.shareit.booking;

import java.util.List;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

public interface BookingService {

    List<BookingDto> getAllBookings();

    BookingDto saveBooking(NewBookingDto booking, Long userId);

    BookingDto getById(Long id, Long userId);

    BookingDto approveBooking(Long bookingId, Long userId, Boolean approved);

    void delete(Long id, Long userId);

    List<BookingDto> getBookingsByBooker(Long bookerId, BookingState state, Integer from, Integer size);

    List<BookingDto> getBookingsByOwner(Long ownerId, BookingState state, Integer from, Integer size);
}