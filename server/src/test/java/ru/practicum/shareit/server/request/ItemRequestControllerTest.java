package ru.practicum.shareit.server.request;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.server.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.exception.NotFoundException;


@WebMvcTest(ItemRequestController.class)
@DisplayName("ItemRequest Controller WebMvc Tests")
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto requestDto1;
    private ItemRequestDto requestDto2;
    private NewItemRequestDto newItemRequestDto;
    private ItemShortDto itemShortDto1;

    private final Long userId1 = 1L;
    private final Long otherUserId = 2L;
    private final Long nonExistentUserId = 99L;
    private final Long request1Id = 10L;
    private final Long request2Id = 11L;
    private final Long nonExistentRequestId = 999L;
    private final Long itemId1 = 100L;
    private final String userIdHeaderName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        newItemRequestDto = new NewItemRequestDto("Need a ladder for painting");

        itemShortDto1 = new ItemShortDto(itemId1, "Ladder A", "Sturdy", true, otherUserId,
                request1Id);

        requestDto1 = new ItemRequestDto(request1Id, "Need a ladder",
                LocalDateTime.now().minusDays(1), Set.of(itemShortDto1));
        requestDto2 = new ItemRequestDto(request2Id, "Need a hammer", LocalDateTime.now(),
                Collections.emptySet());
    }

    @Test
    @DisplayName("POST /requests - Success")
    void addRequest_whenValid_shouldReturnCreatedAndRequestDto() throws Exception {
        when(itemRequestService.addRequest(any(NewItemRequestDto.class), eq(userId1))).thenReturn(
                requestDto1);

        mockMvc.perform(post("/requests")
                        .header(userIdHeaderName, userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/requests/" + request1Id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(request1Id.intValue())))
                .andExpect(jsonPath("$.description", is(requestDto1.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(itemId1.intValue())));


        verify(itemRequestService).addRequest(refEq(newItemRequestDto), eq(userId1));
    }

    @Test
    @DisplayName("POST /requests - Failure (User Not Found)")
    void addRequest_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "User not found";
        when(itemRequestService.addRequest(any(NewItemRequestDto.class), eq(nonExistentUserId)))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(post("/requests")
                        .header(userIdHeaderName, nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemRequestService).addRequest(refEq(newItemRequestDto), eq(nonExistentUserId));
    }

    @Test
    @DisplayName("GET /requests - Success")
    void getOwnRequests_whenUserExists_shouldReturnOkAndRequestList() throws Exception {
        when(itemRequestService.getOwnRequests(userId1)).thenReturn(
                List.of(requestDto1, requestDto2));

        mockMvc.perform(get("/requests")
                        .header(userIdHeaderName, userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(request1Id.intValue())))
                .andExpect(jsonPath("$[1].id", is(request2Id.intValue())));

        verify(itemRequestService).getOwnRequests(userId1);
    }

    @Test
    @DisplayName("GET /requests - Success (No Requests)")
    void getOwnRequests_whenUserHasNoRequests_shouldReturnOkAndEmptyList() throws Exception {
        when(itemRequestService.getOwnRequests(userId1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header(userIdHeaderName, userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemRequestService).getOwnRequests(userId1);
    }

    @Test
    @DisplayName("GET /requests - Failure (User Not Found)")
    void getOwnRequests_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "User not found";
        when(itemRequestService.getOwnRequests(nonExistentUserId))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(get("/requests")
                        .header(userIdHeaderName, nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemRequestService).getOwnRequests(nonExistentUserId);
    }

    @Test
    @DisplayName("GET /requests/all - Success (Defaults)")
    void getAllRequests_whenDefaultParams_shouldReturnOkAndList() throws Exception {
        when(itemRequestService.getAllRequests(eq(userId1), isNull(), isNull()))
                .thenReturn(List.of(requestDto2, requestDto1));

        mockMvc.perform(get("/requests/all")
                        .header(userIdHeaderName, userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(request2Id.intValue())))
                .andExpect(jsonPath("$[1].id", is(request1Id.intValue())));

        verify(itemRequestService).getAllRequests(eq(userId1), isNull(), isNull());
    }

    @Test
    @DisplayName("GET /requests/all?from=0&size=1 - Success (With Paging)")
    void getAllRequests_whenPagingParamsProvided_shouldReturnOkAndPaginatedList() throws Exception {
        int from = 0;
        int size = 1;
        when(itemRequestService.getAllRequests(eq(userId1), eq(from), eq(size)))
                .thenReturn(List.of(requestDto2));

        mockMvc.perform(get("/requests/all")
                        .header(userIdHeaderName, userId1)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(request2Id.intValue())));

        verify(itemRequestService).getAllRequests(eq(userId1), eq(from), eq(size));
    }

    @Test
    @DisplayName("GET /requests/all - Failure (User Not Found)")
    void getAllRequests_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "User not found";
        when(itemRequestService.getAllRequests(eq(nonExistentUserId), any(), any()))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(get("/requests/all")
                        .header(userIdHeaderName, nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemRequestService).getAllRequests(eq(nonExistentUserId), isNull(), isNull());
    }

    @Test
    @DisplayName("GET /requests/{requestId} - Success")
    void getRequestById_whenRequestExists_shouldReturnOkAndDto() throws Exception {
        when(itemRequestService.getRequestById(eq(request1Id), eq(userId1))).thenReturn(
                requestDto1);

        mockMvc.perform(get("/requests/{requestId}", request1Id)
                        .header(userIdHeaderName, userId1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(request1Id.intValue())))
                .andExpect(jsonPath("$.description", is(requestDto1.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(itemId1.intValue())));

        verify(itemRequestService).getRequestById(eq(request1Id), eq(userId1));
    }

    @Test
    @DisplayName("GET /requests/{requestId} - Failure (Request Not Found)")
    void getRequestById_whenRequestNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Request not found";
        when(itemRequestService.getRequestById(eq(nonExistentRequestId), eq(userId1)))
                .thenThrow(new ItemRequestNotFoundException(errorMsg));

        mockMvc.perform(get("/requests/{requestId}", nonExistentRequestId)
                        .header(userIdHeaderName, userId1))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemRequestService).getRequestById(eq(nonExistentRequestId), eq(userId1));
    }

    @Test
    @DisplayName("GET /requests/{requestId} - Failure (User Not Found)")
    void getRequestById_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Requesting user not found";
        when(itemRequestService.getRequestById(eq(request1Id), eq(nonExistentUserId)))
                .thenThrow(new NotFoundException(errorMsg));

        mockMvc.perform(get("/requests/{requestId}", request1Id)
                        .header(userIdHeaderName, nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemRequestService).getRequestById(eq(request1Id), eq(nonExistentUserId));
    }
}