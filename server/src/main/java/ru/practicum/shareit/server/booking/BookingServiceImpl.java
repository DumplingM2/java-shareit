package ru.practicum.shareit.server.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.enums.BookingState;
import ru.practicum.shareit.common.enums.BookingStatus;
import ru.practicum.shareit.server.booking.mapper.BookingMapper;
import ru.practicum.shareit.server.exception.AccessDeniedException;
import ru.practicum.shareit.server.exception.BookingBadRequestException;
import ru.practicum.shareit.server.exception.BookingNotFoundException;
import ru.practicum.shareit.server.exception.ItemNotFoundException;
import ru.practicum.shareit.server.exception.UserNotFoundException;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public List<BookingDto> getAllBookings() {
        List<BookingDto> bookings = bookingRepository.findAll().stream()
                .map(bookingMapper::mapToDto).toList();
        log.debug("Fetched {} bookings", bookings.size());
        return bookings;
    }

    @Override
    public BookingDto saveBooking(NewBookingDto booking, Long userId) {
        User booker = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new UserNotFoundException("User with id " + userId + " not found");
        });
        Item item = itemRepository.findById(booking.getItemId()).orElseThrow(() -> {
            log.warn("Item with id {} not found", booking.getItemId());
            return new ItemNotFoundException(
                    "Item with id " + booking.getItemId() + " not found");
        });
        if (item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} is the owner of item with id {}", userId, booking.getItemId());
            throw new BookingBadRequestException(
                    "User with id " + userId + " is the owner of item with id " + booking.getItemId());
        }
        if (!item.getAvailable()) {
            log.warn("Item with id {} is not available", booking.getItemId());
            throw new BookingBadRequestException(
                    "Item with id " + booking.getItemId() + " is not available");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Booking start time {} is in the past", booking.getStart());
            throw new BookingBadRequestException("Booking start time cannot be in the past");
        }
        if (!booking.getEnd().isAfter(booking.getStart())) {
            log.warn("Booking end time {} is not after start time {}", booking.getEnd(),
                    booking.getStart());
            throw new BookingBadRequestException("Booking end time must be after start time");
        }
        Booking newBooking = bookingMapper.mapToBooking(booking);
        newBooking.setBooker(booker);
        newBooking.setItem(item);
        Booking savedBooking = bookingRepository.save(newBooking);
        log.debug("Saved new booking: {}", savedBooking);
        return bookingMapper.mapToDto(savedBooking);
    }

    @Override
    public BookingDto getById(Long userId, Long id) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException("User with id " + userId + " not found");
        }
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> {
            log.warn("Booking with id {} not found", id);
            return new BookingNotFoundException("Booking with id " + id + " not found");
        });
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("User with id {} is not the booker or owner of booking with id {}", userId, id);
            throw new AccessDeniedException(
                    "User with id " + userId + " is not the booker or owner of booking with id " + id);
        }
        return bookingMapper.mapToDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Booking with id {} not found for {}", bookingId, approved ? "approval" : "rejection");
            return new BookingNotFoundException("Booking with id " + bookingId + " not found");
        });
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("User with id {} is not the owner of item in booking with id {}",
                    userId, bookingId);
            throw new AccessDeniedException(
                    "User with id " + userId + " is not the owner of item in booking with id "
                            + bookingId);
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        log.debug("{} booking: {}", approved ? "Approved" : "Rejected", booking);
        return bookingMapper.mapToDto(booking);
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                    "User with id " + userId + " not found");
        }
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> {
            log.warn("Booking with id {} not found for deletion", id);
            return new BookingNotFoundException("Booking with id " + id + " not found");
        });
        if (!booking.getBooker().getId().equals(userId)) {
            log.warn("Booking with id {} does not belong to user with id {}", id, userId);
            throw new AccessDeniedException(
                    "Booking with id " + id + " does not belong to user with id " + userId);
        }
        log.debug("Deleting booking with id {} by user with id {}", id, userId);
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long bookerId, BookingState state, Integer from, Integer size) {
        if (userRepository.findById(bookerId).isEmpty()) {
            log.warn("User with id {} not found", bookerId);
            throw new UserNotFoundException(
                    "User with id " + bookerId + " not found");
        }
        Pageable pageable = getPageableWithDefaultSort(from, size);
        return bookingRepository.findBookingsByBookerAndState(bookerId, state.name(), LocalDateTime.now(), pageable)
                .stream()
                .map(bookingMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long ownerId, BookingState state, Integer from, Integer size) {
        if (userRepository.findById(ownerId).isEmpty()) {
            log.warn("User with id {} not found", ownerId);
            throw new UserNotFoundException(
                    "User with id " + ownerId + " not found");
        }
        Pageable pageable = getPageableWithDefaultSort(from, size);
        return bookingRepository.findBookingsByItemOwnerAndState(ownerId, state.name(), LocalDateTime.now(), pageable)
                .stream()
                .map(bookingMapper::mapToDto)
                .collect(Collectors.toList());
    }

    private Pageable getPageableWithDefaultSort(Integer from, Integer size) {
        Sort defaultSort = Sort.by("startDate").descending();
        if (from == null || size == null || from < 0 || size <= 0) {
            return Pageable.unpaged();
        }
        return PageRequest.of(from / size, size, defaultSort);
    }
}
