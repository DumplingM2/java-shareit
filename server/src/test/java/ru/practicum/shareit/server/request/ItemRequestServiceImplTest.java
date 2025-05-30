package ru.practicum.shareit.server.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.server.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemRequest Service Implementation Tests")
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Captor
    ArgumentCaptor<ItemRequest> itemRequestCaptor;
    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    private User requestor1;
    private User requestor2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;
    private ItemRequestDto requestDto1;
    private ItemRequestDto requestDto2;
    private ItemRequestDto requestDto3;
    private NewItemRequestDto newItemRequestDto;
    private Item item1;
    private ItemShortDto itemShortDto1;

    private final Long requestor1Id = 1L;
    private final Long requestor2Id = 2L;
    private final Long nonExistentUserId = 99L;
    private final Long request1Id = 10L;
    private final Long request2Id = 11L;
    private final Long request3Id = 12L;
    private final Long nonExistentRequestId = 999L;
    private final Long item1Id = 100L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        requestor1 = new User();
        requestor1.setId(requestor1Id);
        requestor1.setName("Req One");
        requestor1.setEmail("r1@e.com");
        requestor2 = new User();
        requestor2.setId(requestor2Id);
        requestor2.setName("Req Two");
        requestor2.setEmail("r2@e.com");

        newItemRequestDto = new NewItemRequestDto("Need a good hammer");

        item1 = new Item();
        item1.setId(item1Id);
        item1.setName("Hammer Resp");
        item1.setOwner(requestor2);

        request1 = new ItemRequest();
        request1.setId(request1Id);
        request1.setDescription("Need hammer");
        request1.setRequestor(requestor1);
        request1.setCreated(now.minusDays(2));
        request2 = new ItemRequest();
        request2.setId(request2Id);
        request2.setDescription("Need drill");
        request2.setRequestor(requestor1);
        request2.setCreated(now.minusDays(1));
        request3 = new ItemRequest();
        request3.setId(request3Id);
        request3.setDescription("Need ladder");
        request3.setRequestor(requestor2);
        request3.setCreated(now);

        item1.setRequest(request1);
        request1.setItems(Set.of(item1));

        itemShortDto1 = new ItemShortDto(item1Id, "Hammer Resp", null, true, requestor2Id,
                request1Id);
        requestDto1 = new ItemRequestDto(request1Id, "Need hammer", request1.getCreated(),
                Set.of(itemShortDto1));
        requestDto2 = new ItemRequestDto(request2Id, "Need drill", request2.getCreated(),
                Collections.emptySet());
        requestDto3 = new ItemRequestDto(request3Id, "Need ladder", request3.getCreated(),
                Collections.emptySet());
    }

    @Nested
    @DisplayName("addRequest Tests")
    class AddRequestTests {

        @Test
        @DisplayName("should add request and return DTO when user exists")
        void addRequest_whenUserExists_shouldSaveAndReturnDto() {
            ItemRequest requestToSave = new ItemRequest();
            requestToSave.setDescription(newItemRequestDto.getDescription());

            ItemRequest savedRequest = new ItemRequest();
            savedRequest.setId(request1Id);
            savedRequest.setDescription(newItemRequestDto.getDescription());
            savedRequest.setRequestor(requestor1);
            savedRequest.setCreated(LocalDateTime.now());
            savedRequest.setItems(Collections.emptySet());

            ItemRequestDto resultDto = new ItemRequestDto(request1Id,
                    newItemRequestDto.getDescription(), savedRequest.getCreated(),
                    Collections.emptySet());

            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestMapper.mapToEntity(newItemRequestDto)).thenReturn(requestToSave);
            when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);
            when(itemRequestMapper.mapToDto(savedRequest)).thenReturn(resultDto);

            ItemRequestDto actualResult = itemRequestService.addRequest(newItemRequestDto, requestor1Id);

            assertThat("Returned ItemRequestDto should not be null", actualResult,
                    is(notNullValue()));
            assertThat("Returned ItemRequestDto should match the expected DTO", actualResult,
                    equalTo(resultDto));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestMapper).mapToEntity(newItemRequestDto);
            verify(itemRequestRepository).save(itemRequestCaptor.capture());
            ItemRequest captured = itemRequestCaptor.getValue();
            assertThat("Saved ItemRequest entity should have correct requestor and description",
                    captured,
                    allOf(
                            hasProperty("requestor", equalTo(requestor1)),
                            hasProperty("description", equalTo(newItemRequestDto.getDescription()))
                    )
            );
            verify(itemRequestMapper).mapToDto(savedRequest);
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void addRequest_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class,
                    () -> itemRequestService.addRequest(newItemRequestDto, nonExistentUserId),
                    "Adding request when user is not found should throw NotFoundException");

            verify(userRepository).findById(nonExistentUserId);
            verifyNoInteractions(itemRequestMapper, itemRequestRepository);
        }
    }

    @Nested
    @DisplayName("getOwnRequests Tests")
    class GetOwnRequestsTests {

        @Test
        @DisplayName("should return user's requests ordered descending by creation date")
        void getOwnRequests_whenUserExistsAndHasRequests_shouldReturnOrderedDtoList() {
            List<ItemRequest> requestsFromRepo = List.of(request2, request1);
            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestor1Id))
                    .thenReturn(requestsFromRepo);
            when(itemRequestMapper.mapToDto(request1)).thenReturn(requestDto1);
            when(itemRequestMapper.mapToDto(request2)).thenReturn(requestDto2);


            List<ItemRequestDto> results = itemRequestService.getOwnRequests(requestor1Id);

            assertThat("Result list should not be null", results, is(notNullValue()));
            assertThat("Result list should contain 2 items", results, hasSize(2));
            assertThat("Result list should contain the expected ItemRequestDto objects in order", results,
                    contains(requestDto2, requestDto1));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(requestor1Id);
            verify(itemRequestMapper, times(2)).mapToDto(any(ItemRequest.class));
        }

        @Test
        @DisplayName("should return empty list when user has no requests")
        void getOwnRequests_whenUserHasNoRequests_shouldReturnEmptyList() {
            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestor1Id))
                    .thenReturn(Collections.emptyList());

            List<ItemRequestDto> results = itemRequestService.getOwnRequests(requestor1Id);

            assertThat("Result list should not be null", results, is(notNullValue()));
            assertThat("Result list should be empty", results, is(empty()));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(requestor1Id);
            verifyNoInteractions(itemRequestMapper);
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void getOwnRequests_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class,
                    () -> itemRequestService.getOwnRequests(nonExistentUserId),
                    "Getting own requests when user is not found should throw NotFoundException");

            verify(userRepository).findById(nonExistentUserId);
            verifyNoInteractions(itemRequestRepository, itemRequestMapper);
        }
    }

    @Nested
    @DisplayName("getAllRequests Tests")
    class GetAllRequestsTests {
        private final int defaultPage = 0;
        private final int defaultSize = 10;
        private final Sort defaultSort = Sort.by("created").descending();

        @Test
        @DisplayName("should return paginated requests from other users")
        void getAllRequests_whenValidParams_shouldReturnPaginatedList() {
            int from = 0;
            int size = 5;
            Pageable expectedPageable = PageRequest.of(from / size, size, defaultSort);
            Page<ItemRequest> page = new PageImpl<>(List.of(request3), expectedPageable, 1);

            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findAllByRequestorIdNot(eq(requestor1Id),
                    any(Pageable.class))).thenReturn(page);
            when(itemRequestMapper.mapToDto(request3)).thenReturn(requestDto3);

            List<ItemRequestDto> results = itemRequestService.getAllRequests(requestor1Id, from,
                    size);

            assertThat("Result list should not be null", results, is(notNullValue()));
            assertThat("Result list should contain 1 item", results, hasSize(1));
            assertThat("Result list should contain the expected ItemRequestDto object", results,
                    equalTo(List.of(requestDto3)));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findAllByRequestorIdNot(eq(requestor1Id),
                    pageableCaptor.capture());
            Pageable captured = pageableCaptor.getValue();
            assertThat("Captured Pageable should have correct page number",
                    captured.getPageNumber(), equalTo(expectedPageable.getPageNumber()));
            assertThat("Captured Pageable should have correct page size", captured.getPageSize(),
                    equalTo(expectedPageable.getPageSize()));
            assertThat("Captured Pageable should have correct sort", captured.getSort(),
                    equalTo(expectedPageable.getSort()));
            verify(itemRequestMapper).mapToDto(request3);
        }

        @Test
        @DisplayName("should return empty list when no other requests exist")
        void getAllRequests_whenNoOtherRequests_shouldReturnEmptyList() {
            int from = 0;
            int size = 5;
            Pageable expectedPageable = PageRequest.of(from / size, size, defaultSort);
            Page<ItemRequest> emptyPage = Page.empty(expectedPageable);

            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findAllByRequestorIdNot(eq(requestor1Id),
                    any(Pageable.class))).thenReturn(emptyPage);

            List<ItemRequestDto> results = itemRequestService.getAllRequests(requestor1Id, from,
                    size);

            assertThat("Result list should not be null", results, is(notNullValue()));
            assertThat("Result list should be empty", results, is(empty()));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findAllByRequestorIdNot(eq(requestor1Id),
                    any(Pageable.class));
            verifyNoInteractions(itemRequestMapper);
        }

        @Test
        @DisplayName("should use default pagination when parameters are null")
        void getAllRequests_whenPagingParamsNull_shouldUseDefaultPagination() {
            Pageable defaultPageable = PageRequest.of(defaultPage, defaultSize, defaultSort);
            Page<ItemRequest> page = new PageImpl<>(List.of(request3));

            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findAllByRequestorIdNot(eq(requestor1Id), eq(defaultPageable)))
                    .thenReturn(page);
            when(itemRequestMapper.mapToDto(request3)).thenReturn(requestDto3);

            List<ItemRequestDto> results = itemRequestService.getAllRequests(requestor1Id, null,
                    null);

            assertThat("Result list should not be null", results, is(notNullValue()));
            assertThat("Result list should contain 1 item", results, hasSize(1));
            assertThat("Result list should contain the expected ItemRequestDto object", results,
                    equalTo(List.of(requestDto3)));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findAllByRequestorIdNot(eq(requestor1Id), eq(defaultPageable));
            verify(itemRequestMapper).mapToDto(request3);
        }

        @Test
        @DisplayName("should use default pagination when parameters are invalid")
        void getAllRequests_whenPagingParamsInvalid_shouldUseDefaultPagination() {
            Pageable defaultPageable = PageRequest.of(defaultPage, defaultSize, defaultSort);
            Page<ItemRequest> page = new PageImpl<>(List.of(request3));

            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findAllByRequestorIdNot(eq(requestor1Id), eq(defaultPageable)))
                    .thenReturn(page);

            itemRequestService.getAllRequests(requestor1Id, -1, 10);
            itemRequestService.getAllRequests(requestor1Id, 0, 0);
            itemRequestService.getAllRequests(requestor1Id, 0, -5);

            verify(itemRequestRepository, atLeastOnce()).findAllByRequestorIdNot(eq(requestor1Id),
                    eq(defaultPageable));
            verify(itemRequestMapper, times(3)).mapToDto(any(ItemRequest.class));
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void getAllRequests_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class,
                    () -> itemRequestService.getAllRequests(nonExistentUserId, 0, 10),
                    "Getting all requests when user is not found should throw NotFoundException");

            verify(userRepository).findById(nonExistentUserId);
            verifyNoInteractions(itemRequestRepository, itemRequestMapper);
        }
    }

    @Nested
    @DisplayName("getRequestById Tests")
    class GetRequestByIdTests {

        @Test
        @DisplayName("should return request DTO when user and request exist")
        void getRequestById_whenUserAndRequestExist_shouldReturnDto() {
            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findByIdFetchingItems(request1Id)).thenReturn(
                    Optional.of(request1));
            when(itemRequestMapper.mapToDto(request1)).thenReturn(requestDto1);

            ItemRequestDto result = itemRequestService.getRequestById(request1Id, requestor1Id);

            assertThat("Returned ItemRequestDto should not be null", result, is(notNullValue()));
            assertThat("Returned ItemRequestDto should match the expected DTO", result,
                    equalTo(requestDto1));

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findByIdFetchingItems(request1Id);
            verify(itemRequestMapper).mapToDto(request1);
        }

        @Test
        @DisplayName("should throw NotFoundException when user does not exist")
        void getRequestById_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class,
                    () -> itemRequestService.getRequestById(request1Id, nonExistentUserId),
                    "Getting request by ID when user is not found should throw NotFoundException");

            verify(userRepository).findById(nonExistentUserId);
            verifyNoInteractions(itemRequestRepository, itemRequestMapper);
        }

        @Test
        @DisplayName("should throw ItemRequestNotFoundException when request does not exist")
        void getRequestById_whenRequestNotFound_shouldThrowItemRequestNotFoundException() {
            when(userRepository.findById(requestor1Id)).thenReturn(Optional.of(requestor1));
            when(itemRequestRepository.findByIdFetchingItems(nonExistentRequestId)).thenReturn(
                    Optional.empty());
            assertThrows(ItemRequestNotFoundException.class,
                    () -> itemRequestService.getRequestById(nonExistentRequestId, requestor1Id),
                    "Getting non-existent request by ID should throw ItemRequestNotFoundException");

            verify(userRepository).findById(requestor1Id);
            verify(itemRequestRepository).findByIdFetchingItems(nonExistentRequestId);
            verifyNoInteractions(itemRequestMapper);
        }
    }
}