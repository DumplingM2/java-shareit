package ru.practicum.shareit.server.booking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.enums.BookingState;
import ru.practicum.shareit.common.enums.BookingStatus;
import ru.practicum.shareit.server.booking.mapper.BookingMapper;
import ru.practicum.shareit.server.exception.*;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Service Implementation Tests")
class BookingServiceImplTest {

    private final Long ownerId = 1L;
    private final Long bookerId = 2L;
    private final Long itemAvailableId = 10L;
    private final Long itemNotAvailableId = 11L;
    private final Long itemOwnedByBookerId = 12L;
    private final Long bookingWaitingId = 100L;
    private final Long bookingApprovedId = 101L;
    @Captor
    ArgumentCaptor<Booking> bookingArgumentCaptor;
    @Captor
    ArgumentCaptor<Pageable> pageableArgumentCaptor;
    @Captor
    ArgumentCaptor<LocalDateTime> timeArgumentCaptor;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private User owner;
    private User booker;
    private Item itemAvailable;
    private Item itemNotAvailable;
    private Item itemOwnedByBooker;
    private Booking bookingWaiting;
    private Booking bookingApproved;
    private BookingDto bookingDtoWaiting;
    private BookingDto bookingDtoApproved;
    private NewBookingDto newBookingDtoValid;
    private NewBookingDto newBookingDtoStartInPast;
    private NewBookingDto newBookingDtoEndBeforeStart;
    private LocalDateTime now;
    private LocalDateTime startValid;
    private LocalDateTime endValid;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        startValid = now.plusDays(1);
        endValid = now.plusDays(2);

        owner = new User();
        owner.setId(ownerId);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(bookerId);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        itemAvailable = new Item();
        itemAvailable.setId(itemAvailableId);
        itemAvailable.setName("Available Item");
        itemAvailable.setDescription("Desc");
        itemAvailable.setAvailable(true);
        itemAvailable.setOwner(owner);

        itemNotAvailable = new Item();
        itemNotAvailable.setId(itemNotAvailableId);
        itemNotAvailable.setName("Not Available Item");
        itemNotAvailable.setDescription("Desc");
        itemNotAvailable.setAvailable(false);
        itemNotAvailable.setOwner(owner);

        itemOwnedByBooker = new Item();
        itemOwnedByBooker.setId(itemOwnedByBookerId);
        itemOwnedByBooker.setName("Booker Owns This");
        itemOwnedByBooker.setDescription("Desc");
        itemOwnedByBooker.setAvailable(true);
        itemOwnedByBooker.setOwner(booker);

        bookingWaiting = new Booking();
        bookingWaiting.setId(bookingWaitingId);
        bookingWaiting.setStartDate(startValid);
        bookingWaiting.setEndDate(endValid);
        bookingWaiting.setItem(itemAvailable);
        bookingWaiting.setBooker(booker);
        bookingWaiting.setStatus(BookingStatus.WAITING);

        bookingApproved = new Booking();
        bookingApproved.setId(bookingApprovedId);
        bookingApproved.setStartDate(now.minusDays(1));
        bookingApproved.setEndDate(now.plusHours(1));
        bookingApproved.setItem(itemAvailable);
        bookingApproved.setBooker(booker);
        bookingApproved.setStatus(BookingStatus.APPROVED);

        bookingDtoWaiting = new BookingDto(bookingWaitingId, null, null, startValid, endValid,
                BookingStatus.WAITING.toString());
        bookingDtoApproved = new BookingDto(bookingApprovedId, null, null,
                bookingApproved.getStartDate(), bookingApproved.getEndDate(),
                BookingStatus.APPROVED.toString());

        newBookingDtoValid = new NewBookingDto(itemAvailableId, startValid, endValid);
        newBookingDtoStartInPast = new NewBookingDto(itemAvailableId, now.minusDays(1), endValid);
        newBookingDtoEndBeforeStart = new NewBookingDto(itemAvailableId, startValid,
                startValid.minusHours(1));
    }

    @Nested
    @DisplayName("getAllBookings Tests")
    class GetAllBookingsTests {

        @Test
        @DisplayName("should return all bookings")
        void getAllBookings_shouldReturnAllBookingDtos() {
            when(bookingRepository.findAll()).thenReturn(List.of(bookingWaiting, bookingApproved));
            when(bookingMapper.mapToDto(bookingWaiting)).thenReturn(bookingDtoWaiting);
            when(bookingMapper.mapToDto(bookingApproved)).thenReturn(bookingDtoApproved);

            List<BookingDto> result = bookingService.getAllBookings();

            assertThat("Should return a list containing all bookings", result, hasSize(2));
            assertThat("The list should contain the expected booking DTOs", result,
                    containsInAnyOrder(bookingDtoWaiting, bookingDtoApproved));
            verify(bookingRepository).findAll();
            verify(bookingMapper, times(2)).mapToDto(any(Booking.class));
        }

        @Test
        @DisplayName("should return empty list when no bookings")
        void getAllBookings_whenNoBookings_shouldReturnEmptyList() {
            when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

            List<BookingDto> result = bookingService.getAllBookings();

            assertThat("Should return an empty list when no bookings are found", result,
                    is(empty()));
            verify(bookingRepository).findAll();
            verify(bookingMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("saveBooking Tests")
    class SaveBookingTests {

        @Test
        @DisplayName("should save booking and return DTO for valid request")
        void saveBooking_whenValidRequest_shouldSaveAndReturnDto() {
            Booking mappedBooking = new Booking();
            mappedBooking.setStartDate(newBookingDtoValid.getStart());
            mappedBooking.setEndDate(newBookingDtoValid.getEnd());
            mappedBooking.setStatus(BookingStatus.WAITING);

            Booking savedBooking = new Booking();
            savedBooking.setId(bookingWaitingId);
            savedBooking.setStartDate(newBookingDtoValid.getStart());
            savedBooking.setEndDate(newBookingDtoValid.getEnd());
            savedBooking.setItem(itemAvailable);
            savedBooking.setBooker(booker);
            savedBooking.setStatus(BookingStatus.WAITING);

            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(itemRepository.findById(newBookingDtoValid.getItemId())).thenReturn(
                    Optional.of(itemAvailable));
            when(bookingMapper.mapToBooking(newBookingDtoValid)).thenReturn(mappedBooking);
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingMapper.mapToDto(savedBooking)).thenReturn(bookingDtoWaiting);

            BookingDto result = bookingService.saveBooking(newBookingDtoValid, bookerId);

            assertThat("The returned BookingDto should match the expected DTO", result,
                    equalTo(bookingDtoWaiting));
            verify(userRepository).findById(bookerId);
            verify(itemRepository).findById(itemAvailableId);
            verify(bookingMapper).mapToBooking(newBookingDtoValid);
            verify(bookingRepository).save(bookingArgumentCaptor.capture());
            Booking capturedBooking = bookingArgumentCaptor.getValue();
            assertThat("Saved booking should have the correct booker", capturedBooking.getBooker(),
                    equalTo(booker));
            assertThat("Saved booking should have the correct item", capturedBooking.getItem(),
                    equalTo(itemAvailable));
            assertThat("Saved booking should have WAITING status", capturedBooking.getStatus(),
                    equalTo(BookingStatus.WAITING));
            verify(bookingMapper).mapToDto(savedBooking);
        }

        @Test
        @DisplayName("should throw NotFoundException when booker not found")
        void saveBooking_whenBookerNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.saveBooking(newBookingDtoValid, bookerId),
                    "Should throw NotFoundException when booker ID does not exist");

            verify(userRepository).findById(bookerId);
            verifyNoInteractions(itemRepository, bookingMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw NotFoundException when item not found")
        void saveBooking_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(itemRepository.findById(newBookingDtoValid.getItemId())).thenReturn(
                    Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.saveBooking(newBookingDtoValid, bookerId),
                    "Should throw NotFoundException when item ID does not exist");

            verify(userRepository).findById(bookerId);
            verify(itemRepository).findById(itemAvailableId);
            verifyNoInteractions(bookingMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw BookingBadRequestException when owner tries to book own item")
        void saveBooking_whenOwnerBooksOwnItem_shouldThrowBookingBadRequestException() {
            NewBookingDto ownerBookingDto = new NewBookingDto(itemOwnedByBookerId, startValid,
                    endValid);
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(itemRepository.findById(itemOwnedByBookerId)).thenReturn(
                    Optional.of(itemOwnedByBooker));

            assertThrows(BookingBadRequestException.class,
                    () -> bookingService.saveBooking(ownerBookingDto, bookerId),
                    "Should throw BookingBadRequestException when booker is the item owner");

            verify(userRepository).findById(bookerId);
            verify(itemRepository).findById(itemOwnedByBookerId);
            verifyNoInteractions(bookingMapper, bookingRepository);
        }


        @Test
        @DisplayName("should throw BookingBadRequestException when item is not available")
        void saveBooking_whenItemNotAvailable_shouldThrowBookingBadRequestException() {
            NewBookingDto bookingUnavailableDto = new NewBookingDto(itemNotAvailableId, startValid,
                    endValid);
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(itemRepository.findById(itemNotAvailableId)).thenReturn(
                    Optional.of(itemNotAvailable));

            assertThrows(BookingBadRequestException.class,
                    () -> bookingService.saveBooking(bookingUnavailableDto, bookerId),
                    "Should throw BookingBadRequestException when item is not available");

            verify(userRepository).findById(bookerId);
            verify(itemRepository).findById(itemNotAvailableId);
            verifyNoInteractions(bookingMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw BookingBadRequestException when start time is in the past")
        void saveBooking_whenStartTimeInPast_shouldThrowBookingBadRequestException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(itemRepository.findById(itemAvailableId)).thenReturn(Optional.of(itemAvailable));

            assertThrows(BookingBadRequestException.class,
                    () -> bookingService.saveBooking(newBookingDtoStartInPast, bookerId),
                    "Should throw BookingBadRequestException when booking start time is in the past");

            verify(userRepository).findById(bookerId);
            verify(itemRepository).findById(itemAvailableId);
            verifyNoInteractions(bookingMapper, bookingRepository);
        }


        @Test
        @DisplayName(
                "should throw BookingBadRequestException when end time is not after start " + "time")
        void saveBooking_whenEndTimeNotAfterStart_shouldThrowBookingBadRequestException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(itemRepository.findById(itemAvailableId)).thenReturn(Optional.of(itemAvailable));

            assertThrows(BookingBadRequestException.class,
                    () -> bookingService.saveBooking(newBookingDtoEndBeforeStart, bookerId),
                    "Should throw BookingBadRequestException when booking end time is not after start"
                            + " time");

            verify(userRepository).findById(bookerId);
            verify(itemRepository).findById(itemAvailableId);
            verifyNoInteractions(bookingMapper, bookingRepository);
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("should return BookingDto when requested by Booker")
        void getById_whenRequestedByBooker_shouldReturnDto() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));
            when(bookingMapper.mapToDto(bookingWaiting)).thenReturn(bookingDtoWaiting);

            BookingDto result = bookingService.getById(bookerId, bookingWaitingId);

            assertThat("Should return the booking DTO when requested by the booker", result,
                    equalTo(bookingDtoWaiting));
            verify(userRepository).findById(bookerId);
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingMapper).mapToDto(bookingWaiting);
        }

        @Test
        @DisplayName("should return BookingDto when requested by Owner")
        void getById_whenRequestedByOwner_shouldReturnDto() {
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));
            when(bookingMapper.mapToDto(bookingWaiting)).thenReturn(bookingDtoWaiting);

            BookingDto result = bookingService.getById(ownerId, bookingWaitingId);

            assertThat("Should return the booking DTO when requested by the owner", result,
                    equalTo(bookingDtoWaiting));
            verify(userRepository).findById(ownerId);
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingMapper).mapToDto(bookingWaiting);
        }

        @Test
        @DisplayName("should throw NotFoundException when requesting user not found")
        void getById_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.getById(bookerId, bookingWaitingId),
                    "Should throw NotFoundException when requesting user is not found");

            verify(userRepository).findById(bookerId);
            verifyNoInteractions(bookingRepository, bookingMapper);
        }

        @Test
        @DisplayName("should throw NotFoundException when booking not found")
        void getById_whenBookingNotFound_shouldThrowBookingNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.getById(bookerId, bookingWaitingId),
                    "Should throw NotFoundException when booking is not found");

            verify(userRepository).findById(bookerId);
            verify(bookingRepository).findById(bookingWaitingId);
            verifyNoInteractions(bookingMapper);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is neither Booker nor Owner")
        void getById_whenUserNotBookerOrOwner_shouldThrowAccessDeniedException() {
            User unrelatedUser = new User();
            unrelatedUser.setId(3L);
            when(userRepository.findById(unrelatedUser.getId())).thenReturn(
                    Optional.of(unrelatedUser));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));

            assertThrows(AccessDeniedException.class,
                    () -> bookingService.getById(unrelatedUser.getId(), bookingWaitingId),
                    "Should throw AccessDeniedException when user is not the booker or owner");

            verify(userRepository).findById(unrelatedUser.getId());
            verify(bookingRepository).findById(bookingWaitingId);
            verifyNoInteractions(bookingMapper);
        }
    }

    @Nested
    @DisplayName("approveBooking Tests")
    class ApproveBookingTests {

        @Test
        @DisplayName("should approve booking when owner approves")
        void approveBooking_whenApproveTrueAndUserIsOwner_shouldSetStatusApproved() {
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));
            when(bookingMapper.mapToDto(any(Booking.class))).thenReturn(bookingDtoApproved);

            BookingDto result = bookingService.approveBooking(bookingWaitingId, ownerId, true);

            assertThat("Booking status should be APPROVED after owner approves", result.getStatus(),
                    equalTo(BookingStatus.APPROVED.toString()));
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingRepository).save(bookingArgumentCaptor.capture());
            Booking savedBooking = bookingArgumentCaptor.getValue();
            assertThat("Saved booking status in repository should be APPROVED",
                    savedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
            verify(bookingMapper).mapToDto(savedBooking);
        }

        @Test
        @DisplayName("should reject booking when owner rejects")
        void approveBooking_whenApproveFalseAndUserIsOwner_shouldSetStatusRejected() {
            BookingDto rejectedDto = new BookingDto(bookingWaitingId, null, null, startValid,
                    endValid, BookingStatus.REJECTED.toString());
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));
            when(bookingMapper.mapToDto(any(Booking.class))).thenReturn(rejectedDto);

            BookingDto result = bookingService.approveBooking(bookingWaitingId, ownerId, false);

            assertThat("Booking status should be REJECTED after owner rejects", result.getStatus(),
                    equalTo(BookingStatus.REJECTED.toString()));
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingRepository).save(bookingArgumentCaptor.capture());
            Booking savedBooking = bookingArgumentCaptor.getValue();
            assertThat("Saved booking status in repository should be REJECTED",
                    savedBooking.getStatus(), equalTo(BookingStatus.REJECTED));
            verify(bookingMapper).mapToDto(savedBooking);
        }

        @Test
        @DisplayName("should throw NotFoundException when booking not found")
        void approveBooking_whenBookingNotFound_shouldThrowBookingNotFoundException() {
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.approveBooking(bookingWaitingId, ownerId, true),
                    "Should throw NotFoundException when booking is not found");

            verify(bookingRepository).findById(bookingWaitingId);
            verifyNoInteractions(bookingMapper);
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not owner")
        void approveBooking_whenUserNotOwner_shouldThrowAccessDeniedException() {
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));

            assertThrows(AccessDeniedException.class,
                    () -> bookingService.approveBooking(bookingWaitingId, bookerId, true),
                    "Should throw AccessDeniedException when user is not the owner of the item");

            verify(bookingRepository).findById(bookingWaitingId);
            verifyNoInteractions(bookingMapper);
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete booking when user is booker")
        void delete_whenUserIsBooker_shouldDeleteBooking() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));

            assertDoesNotThrow(() -> bookingService.delete(bookingWaitingId, bookerId),
                    "Should not throw an exception when booker deletes their booking");

            verify(userRepository).findById(bookerId);
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingRepository).deleteById(bookingWaitingId);
        }

        @Test
        @DisplayName("should throw NotFoundException when user not found")
        void delete_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.delete(bookingWaitingId, bookerId),
                    "Should throw NotFoundException when user is not found");

            verify(userRepository).findById(bookerId);
            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("should throw NotFoundException when booking not found")
        void delete_whenBookingNotFound_shouldThrowBookingNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> bookingService.delete(bookingWaitingId, bookerId),
                    "Should throw NotFoundException when booking is not found");

            verify(userRepository).findById(bookerId);
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not booker")
        void delete_whenUserIsNotBooker_shouldThrowAccessDeniedException() {
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(bookingRepository.findById(bookingWaitingId)).thenReturn(
                    Optional.of(bookingWaiting));

            assertThrows(AccessDeniedException.class,
                    () -> bookingService.delete(bookingWaitingId, ownerId),
                    "Should throw AccessDeniedException when user is not the booker");

            verify(userRepository).findById(ownerId);
            verify(bookingRepository).findById(bookingWaitingId);
            verify(bookingRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("getBookingsBy[Booker/Owner] Tests")
    class GetBookingsByRoleTests {

        private final int from = 0;
        private final int size = 10;
        private final Sort defaultSort = Sort.by("startDate").descending();

        @Test
        @DisplayName("getBookingsByBooker should call repository with correct parameters")
        void getBookingsByBooker_shouldCallRepositoryCorrectly() {
            Page<Booking> page = new PageImpl<>(List.of(bookingWaiting),
                    PageRequest.of(0, size, defaultSort), 1);
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findBookingsByBookerAndState(eq(bookerId),
                    eq(BookingState.WAITING.name()), any(LocalDateTime.class),
                    any(Pageable.class))).thenReturn(page);
            when(bookingMapper.mapToDto(bookingWaiting)).thenReturn(bookingDtoWaiting);

            List<BookingDto> result = bookingService.getBookingsByBooker(bookerId,
                    BookingState.WAITING, from, size);

            assertThat("Should return a list with one booking DTO", result, hasSize(1));
            assertThat("The returned booking DTO should be the expected one", result.getFirst(),
                    equalTo(bookingDtoWaiting));
            verify(userRepository).findById(bookerId);
            verify(bookingRepository).findBookingsByBookerAndState(eq(bookerId),
                    eq(BookingState.WAITING.name()), timeArgumentCaptor.capture(),
                    pageableArgumentCaptor.capture());
            Pageable capturedPageable = pageableArgumentCaptor.getValue();
            assertThat("Captured page number should be correct", capturedPageable.getPageNumber(),
                    equalTo(from / size));
            assertThat("Captured page size should be correct", capturedPageable.getPageSize(),
                    equalTo(size));
            assertThat("Captured sort should be correct", capturedPageable.getSort(),
                    equalTo(defaultSort));
            verify(bookingMapper).mapToDto(bookingWaiting);
        }

        @Test
        @DisplayName("getBookingsByOwner should call repository with correct parameters")
        void getBookingsByOwner_shouldCallRepositoryCorrectly() {
            Page<Booking> page = new PageImpl<>(List.of(bookingWaiting),
                    PageRequest.of(0, size, defaultSort), 1);
            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(bookingRepository.findBookingsByItemOwnerAndState(eq(ownerId),
                    eq(BookingState.WAITING.name()), any(LocalDateTime.class),
                    any(Pageable.class))).thenReturn(page);
            when(bookingMapper.mapToDto(bookingWaiting)).thenReturn(bookingDtoWaiting);

            List<BookingDto> result = bookingService.getBookingsByOwner(ownerId,
                    BookingState.WAITING, from, size);

            assertThat("Should return a list with one booking DTO", result, hasSize(1));
            assertThat("The returned booking DTO should be the expected one", result.getFirst(),
                    equalTo(bookingDtoWaiting));
            verify(userRepository).findById(ownerId);
            verify(bookingRepository).findBookingsByItemOwnerAndState(eq(ownerId),
                    eq(BookingState.WAITING.name()), timeArgumentCaptor.capture(),
                    pageableArgumentCaptor.capture());
            Pageable capturedPageable = pageableArgumentCaptor.getValue();
            assertThat("Captured page number should be correct", capturedPageable.getPageNumber(),
                    equalTo(from / size));
            assertThat("Captured page size should be correct", capturedPageable.getPageSize(),
                    equalTo(size));
            assertThat("Captured sort should be correct", capturedPageable.getSort(),
                    equalTo(defaultSort));
            verify(bookingMapper).mapToDto(bookingWaiting);
        }

        @Test
        @DisplayName("getBookingsByBooker should use unpaged when from/size invalid")
        void getBookingsByBooker_whenPagingInvalid_shouldUseUnpaged() {
            Page<Booking> page = new PageImpl<>(List.of(bookingWaiting));
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findBookingsByBookerAndState(eq(bookerId),
                    eq(BookingState.ALL.name()), any(LocalDateTime.class),
                    eq(Pageable.unpaged()))).thenReturn(page);
            when(bookingMapper.mapToDto(bookingWaiting)).thenReturn(bookingDtoWaiting);

            bookingService.getBookingsByBooker(bookerId, BookingState.ALL, null, size);
            verify(bookingRepository).findBookingsByBookerAndState(anyLong(), anyString(),
                    any(LocalDateTime.class), eq(Pageable.unpaged()));

            bookingService.getBookingsByBooker(bookerId, BookingState.ALL, -1, size);
            verify(bookingRepository, times(2)).findBookingsByBookerAndState(anyLong(), anyString(),
                    any(LocalDateTime.class), eq(Pageable.unpaged()));

            bookingService.getBookingsByBooker(bookerId, BookingState.ALL, from, 0);
            verify(bookingRepository, times(3)).findBookingsByBookerAndState(anyLong(), anyString(),
                    any(LocalDateTime.class), eq(Pageable.unpaged()));
        }

        @Test
        @DisplayName("getBookingsByBooker should throw NotFoundException")
        void getBookingsByBooker_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(bookerId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class,
                    () -> bookingService.getBookingsByBooker(bookerId, BookingState.ALL, from, size),
                    "Should throw NotFoundException when booker is not found");
            verifyNoInteractions(bookingRepository, bookingMapper);
        }

        @Test
        @DisplayName("getBookingsByOwner should throw NotFoundException")
        void getBookingsByOwner_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class,
                    () -> bookingService.getBookingsByOwner(ownerId, BookingState.ALL, from, size),
                    "Should throw NotFoundException when owner is not found");
            verifyNoInteractions(bookingRepository, bookingMapper);
        }

        @Test
        @DisplayName(
                "getBookingsByBooker should return empty list when repository returns empty " + "page")
        void getBookingsByBooker_whenNoBookingsMatch_shouldReturnEmptyList() {
            Page<Booking> emptyPage = Page.empty();
            when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
            when(bookingRepository.findBookingsByBookerAndState(anyLong(), anyString(),
                    any(LocalDateTime.class), any(Pageable.class))).thenReturn(emptyPage);

            List<BookingDto> result = bookingService.getBookingsByBooker(bookerId, BookingState.ALL,
                    from, size);

            assertThat("Should return an empty list when no bookings match the criteria", result,
                    is(empty()));
            verify(bookingMapper, never()).mapToDto(any());
        }
    }
}