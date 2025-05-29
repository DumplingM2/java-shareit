package ru.practicum.shareit.server.booking;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.dto.booking.BookingDto;
import ru.practicum.shareit.common.dto.booking.NewBookingDto;
import ru.practicum.shareit.common.enums.BookingState;
import ru.practicum.shareit.common.enums.BookingStatus;
import ru.practicum.shareit.server.exception.*;

@WebMvcTest(BookingController.class)
@DisplayName("Booking Controller WebMvc Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    private BookingDto bookingDto1;
    private BookingDto bookingDto2;
    private NewBookingDto newBookingDto;

    private final Long ownerId = 1L;
    private final Long bookerId = 2L;
    private final Long otherUserId = 3L;
    private final Long itemId = 10L;
    private final Long booking1Id = 100L;
    private final Long booking2Id = 101L;
    private final Long nonExistentBookingId = 999L;
    private final String userIdHeaderName = "X-Sharer-User-Id";
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS);
        end = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS);

        newBookingDto = new NewBookingDto(itemId, start, end);

        bookingDto1 = new BookingDto(booking1Id, null, null, start, end,
                BookingStatus.WAITING.toString());
        bookingDto2 = new BookingDto(booking2Id, null, null, start.plusDays(5), end.plusDays(5),
                BookingStatus.APPROVED.toString());
    }

    @Test
    @DisplayName("POST /bookings - Success")
    void saveBooking_whenValid_shouldReturnCreatedAndBookingDto() throws Exception {
        when(bookingService.saveBooking(any(NewBookingDto.class), eq(bookerId))).thenReturn(
                bookingDto1);

        mockMvc.perform(post("/bookings").header(userIdHeaderName, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookingDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/bookings/" + booking1Id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(booking1Id.intValue())))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.toString())));

        verify(bookingService).saveBooking(refEq(newBookingDto), eq(bookerId));
    }

    @Test
    @DisplayName("POST /bookings - Failure (Booker Not Found)")
    void saveBooking_whenBookerNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Booker not found";
        when(bookingService.saveBooking(any(NewBookingDto.class), eq(otherUserId)))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(post("/bookings")
                        .header(userIdHeaderName, otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookingDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(bookingService).saveBooking(refEq(newBookingDto), eq(otherUserId));
    }

    @Test
    @DisplayName("POST /bookings - Failure (Item Not Found)")
    void saveBooking_whenItemNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Item not found";
        when(bookingService.saveBooking(any(NewBookingDto.class), eq(bookerId)))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(post("/bookings")
                        .header(userIdHeaderName, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookingDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(bookingService).saveBooking(refEq(newBookingDto), eq(bookerId));
    }

    @Test
    @DisplayName("POST /bookings - Failure (Item Not Available)")
    void saveBooking_whenItemNotAvailable_shouldReturnBadRequest() throws Exception {
        String errorMsg = "Item not available";
        when(bookingService.saveBooking(any(NewBookingDto.class), eq(bookerId)))
                .thenThrow(new BookingBadRequestException(errorMsg));

        mockMvc.perform(post("/bookings")
                        .header(userIdHeaderName, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(400)));

        verify(bookingService).saveBooking(refEq(newBookingDto), eq(bookerId));
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId}?approved=true - Success")
    void approveBooking_whenApproveTrue_shouldReturnOkAndApprovedDto() throws Exception {
        BookingDto approvedDto = new BookingDto(booking1Id, null, null, start, end,
                BookingStatus.APPROVED.toString());
        when(bookingService.approveBooking(eq(booking1Id), eq(ownerId), eq(true))).thenReturn(
                approvedDto);

        mockMvc.perform(patch("/bookings/{bookingId}", booking1Id)
                        .header(userIdHeaderName, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(booking1Id.intValue())))
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));

        verify(bookingService).approveBooking(eq(booking1Id), eq(ownerId), eq(true));
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId}?approved=false - Success")
    void approveBooking_whenApproveFalse_shouldReturnOkAndRejectedDto() throws Exception {
        BookingDto rejectedDto = new BookingDto(booking1Id, null, null, start, end,
                BookingStatus.REJECTED.toString());
        when(bookingService.approveBooking(eq(booking1Id), eq(ownerId), eq(false))).thenReturn(
                rejectedDto);

        mockMvc.perform(patch("/bookings/{bookingId}", booking1Id)
                        .header(userIdHeaderName, ownerId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(booking1Id.intValue())))
                .andExpect(jsonPath("$.status", is(BookingStatus.REJECTED.toString())));

        verify(bookingService).approveBooking(eq(booking1Id), eq(ownerId), eq(false));
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} - Failure (Booking Not Found)")
    void approveBooking_whenBookingNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Booking not found";
        when(bookingService.approveBooking(eq(nonExistentBookingId), eq(ownerId), eq(true)))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(patch("/bookings/{bookingId}", nonExistentBookingId)
                        .header(userIdHeaderName, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(bookingService).approveBooking(eq(nonExistentBookingId), eq(ownerId), eq(true));
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} - Failure (User Not Owner)")
    void approveBooking_whenUserNotOwner_shouldReturnForbidden() throws Exception {
        String errorMsg = "User is not owner";
        when(bookingService.approveBooking(eq(booking1Id), eq(bookerId), eq(true)))
                .thenThrow(new AccessDeniedException(errorMsg));

        mockMvc.perform(patch("/bookings/{bookingId}", booking1Id)
                        .header(userIdHeaderName, bookerId)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(403)));

        verify(bookingService).approveBooking(eq(booking1Id), eq(bookerId), eq(true));
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - Success (Requested by Booker)")
    void getById_whenRequestedByBooker_shouldReturnOkAndDto() throws Exception {
        when(bookingService.getById(eq(bookerId), eq(booking1Id))).thenReturn(bookingDto1);

        mockMvc.perform(get("/bookings/{bookingId}", booking1Id)
                        .header(userIdHeaderName, bookerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(booking1Id.intValue())));

        verify(bookingService).getById(eq(bookerId), eq(booking1Id));
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - Success (Requested by Owner)")
    void getById_whenRequestedByOwner_shouldReturnOkAndDto() throws Exception {
        when(bookingService.getById(eq(ownerId), eq(booking1Id))).thenReturn(bookingDto1);

        mockMvc.perform(get("/bookings/{bookingId}", booking1Id)
                        .header(userIdHeaderName, ownerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(booking1Id.intValue())));

        verify(bookingService).getById(eq(ownerId), eq(booking1Id));
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - Failure (Booking Not Found)")
    void getById_whenBookingNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Booking not found";
        when(bookingService.getById(eq(bookerId), eq(nonExistentBookingId)))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(get("/bookings/{bookingId}", nonExistentBookingId)
                        .header(userIdHeaderName, bookerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(bookingService).getById(eq(bookerId), eq(nonExistentBookingId));
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - Failure (User Not Booker or Owner)")
    void getById_whenUserNotBookerOrOwner_shouldReturnForbidden() throws Exception {
        String errorMsg = "User not authorized";
        when(bookingService.getById(eq(otherUserId), eq(booking1Id)))
                .thenThrow(new AccessDeniedException(errorMsg));

        mockMvc.perform(get("/bookings/{bookingId}", booking1Id)
                        .header(userIdHeaderName, otherUserId))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(403)));

        verify(bookingService).getById(eq(otherUserId), eq(booking1Id));
    }

    @Test
    @DisplayName("GET /bookings?state=ALL - Success")
    void getBookingsByBooker_whenDefaultState_shouldReturnOkAndList() throws Exception {
        when(bookingService.getBookingsByBooker(eq(bookerId), eq(BookingState.ALL), any(), any()))
                .thenReturn(List.of(bookingDto1, bookingDto2));

        mockMvc.perform(get("/bookings")
                        .header(userIdHeaderName, bookerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(booking1Id.intValue())))
                .andExpect(jsonPath("$[1].id", is(booking2Id.intValue())));

        verify(bookingService).getBookingsByBooker(eq(bookerId), eq(BookingState.ALL), isNull(),
                isNull());
    }

    @Test
    @DisplayName("GET /bookings?state=WAITING&from=0&size=1 - Success")
    void getBookingsByBooker_whenStateAndPaging_shouldReturnOkAndList() throws Exception {
        int from = 0;
        int size = 1;
        when(bookingService.getBookingsByBooker(eq(bookerId), eq(BookingState.WAITING), eq(from),
                eq(size))).thenReturn(List.of(bookingDto1));

        mockMvc.perform(get("/bookings")
                        .header(userIdHeaderName, bookerId)
                        .param("state", "WAITING")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(booking1Id.intValue())));

        verify(bookingService).getBookingsByBooker(eq(bookerId), eq(BookingState.WAITING), eq(from),
                eq(size));
    }

    @Test
    @DisplayName("GET /bookings - Failure (Booker Not Found)")
    void getBookingsByBooker_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Booker user not found";
        when(bookingService.getBookingsByBooker(eq(nonExistentBookingId), any(BookingState.class),
                any(), any())).thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(get("/bookings")
                        .header(userIdHeaderName, nonExistentBookingId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(bookingService).getBookingsByBooker(eq(nonExistentBookingId), eq(BookingState.ALL),
                isNull(), isNull());
    }

    @Test
    @DisplayName("GET /bookings/owner?state=ALL - Success")
    void getBookingsByOwner_whenDefaultState_shouldReturnOkAndList() throws Exception {
        when(bookingService.getBookingsByOwner(eq(ownerId), eq(BookingState.ALL), any(),
                any())).thenReturn(List.of(bookingDto1, bookingDto2));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeaderName, ownerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(booking1Id.intValue())))
                .andExpect(jsonPath("$[1].id", is(booking2Id.intValue())));

        verify(bookingService).getBookingsByOwner(eq(ownerId), eq(BookingState.ALL), isNull(),
                isNull());
    }

    @Test
    @DisplayName("GET /bookings/owner - Failure (Owner Not Found)")
    void getBookingsByOwner_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Owner user not found";
        when(bookingService.getBookingsByOwner(eq(nonExistentBookingId), any(BookingState.class),
                any(), any())).thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeaderName, nonExistentBookingId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(bookingService).getBookingsByOwner(eq(nonExistentBookingId), eq(BookingState.ALL),
                isNull(), isNull());
    }
}