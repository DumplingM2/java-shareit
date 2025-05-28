package ru.practicum.shareit.server.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String s) {
        super(s);
    }
}
