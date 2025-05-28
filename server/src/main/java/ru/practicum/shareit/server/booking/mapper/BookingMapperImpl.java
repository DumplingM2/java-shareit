package ru.practicum.shareit.server.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.enums.BookingStatus;
import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.server.booking.Booking;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.user.mapper.UserMapper;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BookingMapperImpl implements BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public BookingDto mapToDto(Booking booking) {
        return new BookingDto(booking.getId(), itemMapper.mapToDto(booking.getItem()), userMapper.mapToDto(booking.getBooker()),
                booking.getStartDate(), booking.getEndDate(), booking.getStatus().toString());
    }

    @Override
    public Booking mapToBooking(NewBookingDto newBookingDto) {
        Booking booking = new Booking();
        booking.setStartDate(newBookingDto.getStart());
        booking.setEndDate(newBookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}