package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> saveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @RequestBody @Valid NewBookingDto booking) {
        log.info("Processing request to create a new booking...");
        BookingDto savedBooking = bookingService.saveBooking(booking, userId);
        return ResponseEntity.created(URI.create("/bookings/" + savedBooking.getId()))
                .body(savedBooking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                                     @PathVariable Long bookingId, @RequestParam Boolean approved) {
        log.info("Processing request to {} booking with id {}", approved ? "approve" : "reject", bookingId);
        return ResponseEntity.ok(bookingService.approveBooking(bookingId, userId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getById(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long bookingId) {
        log.info("Processing request to fetch booking by id: {}", bookingId);
        return ResponseEntity.ok(bookingService.getById(userId, bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsByBooker(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState state,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", required = false) Integer size) {
        log.info("Processing request to fetch {} bookings by booker with id: {}", state.name().toLowerCase(), userId);
        return ResponseEntity.ok(bookingService.getBookingsByBooker(userId, state, from, size));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsByOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState state,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", required = false) Integer size) {
        log.info("Processing request to fetch {} bookings by item owner with id: {}", state.name().toLowerCase(), userId);
        return ResponseEntity.ok(bookingService.getBookingsByOwner(userId, state, from, size));
    }
}