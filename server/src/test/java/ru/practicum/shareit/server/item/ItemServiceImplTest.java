package ru.practicum.shareit.server.item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import ru.practicum.shareit.common.dto.booking.BookingShortDto;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.exception.AccessDeniedException;
import ru.practicum.shareit.server.exception.BookingBadRequestException;
import ru.practicum.shareit.server.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.mapper.CommentMapper;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Item Service Implementation Tests")
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    ArgumentCaptor<Comment> commentArgumentCaptor;
    @Captor
    ArgumentCaptor<LocalDateTime> timeArgumentCaptor;

    private User ownerUser;
    private User otherUser;
    private Item item1;
    private Item item2;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private ItemWithBookingInfoDto itemWithBookingInfoDto1;
    private NewItemDto newItemDto;
    private NewItemDto newItemDtoWithRequest;
    private UpdateItemDto updateItemDto;
    private Comment comment1;
    private CommentDto commentDto1;
    private NewCommentDto newCommentDto;
    private BookingShortDto lastBookingDto;
    private BookingShortDto nextBookingDto;
    private ItemRequest itemRequest1;

    private final Long ownerUserId = 1L;
    private final Long otherUserId = 2L;
    private final Long item1Id = 10L;
    private final Long item2Id = 11L;
    private final Long comment1Id = 100L;
    private final Long lastBookingId = 200L;
    private final Long nextBookingId = 201L;
    private final Long itemRequestId = 50L;
    private final Long nonExistentRequestId = 55L;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(ownerUserId);
        ownerUser.setName("Owner");
        ownerUser.setEmail("owner@example.com");

        otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");

        itemRequest1 = new ItemRequest();
        itemRequest1.setId(itemRequestId);
        itemRequest1.setDescription("Need stuff");
        itemRequest1.setRequestor(otherUser);
        itemRequest1.setCreated(LocalDateTime.now().minusDays(1));

        item1 = new Item();
        item1.setId(item1Id);
        item1.setName("Item One");
        item1.setDescription("Desc One");
        item1.setAvailable(true);
        item1.setOwner(ownerUser);
        item1.setComments(Collections.emptySet());

        item2 = new Item();
        item2.setId(item2Id);
        item2.setName("Item Two");
        item2.setDescription("Desc Two");
        item2.setAvailable(false);
        item2.setOwner(ownerUser);
        item2.setComments(Collections.emptySet());

        itemDto1 = new ItemDto(item1Id, "Item One", "Desc One", true);
        itemDto2 = new ItemDto(item2Id, "Item Two", "Desc Two", false);

        itemWithBookingInfoDto1 = new ItemWithBookingInfoDto(item1Id, "Item One", "Desc One", true,
                Collections.emptySet(), null, null);

        newItemDto = new NewItemDto();
        newItemDto.setName("New Item");
        newItemDto.setDescription("New Desc");
        newItemDto.setAvailable(true);
        newItemDto.setRequestId(null);

        newItemDtoWithRequest = new NewItemDto();
        newItemDtoWithRequest.setName("Item for Request");
        newItemDtoWithRequest.setDescription("Specific Desc");
        newItemDtoWithRequest.setAvailable(true);
        newItemDtoWithRequest.setRequestId(itemRequestId);

        updateItemDto = new UpdateItemDto("Updated Name", "Updated Desc", false);

        comment1 = new Comment();
        comment1.setId(comment1Id);
        comment1.setText("Test Comment");
        comment1.setItem(item1);
        comment1.setAuthor(otherUser);
        comment1.setCreated(LocalDateTime.now().minusDays(1));

        commentDto1 = new CommentDto(comment1Id, "Test Comment", item1Id, otherUser.getName(),
                comment1.getCreated().toString());

        newCommentDto = new NewCommentDto();
        newCommentDto.setText("A new comment");

        lastBookingDto = new BookingShortDto(lastBookingId, otherUserId, item1Id,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        nextBookingDto = new BookingShortDto(nextBookingId, otherUserId, item1Id,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
    }

    @Nested
    @DisplayName("getAllItems Tests")
    class GetAllItemsTests {

        @Test
        @DisplayName("should return all items")
        void getAllItems_shouldReturnAllItemDtos() {
            when(itemRepository.findAll()).thenReturn(List.of(item1, item2));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);
            when(itemMapper.mapToDto(item2)).thenReturn(itemDto2);

            List<ItemDto> result = itemService.getAllItems();

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should contain 2 items", result, hasSize(2));
            assertThat("Result list should contain the expected ItemDto objects", result,
                    contains(itemDto1, itemDto2));

            verify(itemRepository).findAll();
            verify(itemMapper, times(2)).mapToDto(any(Item.class));
        }

        @Test
        @DisplayName("should return empty list when no items")
        void getAllItems_whenNoItems_shouldReturnEmptyList() {
            when(itemRepository.findAll()).thenReturn(Collections.emptyList());

            List<ItemDto> result = itemService.getAllItems();

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should be empty", result, is(empty()));

            verify(itemRepository).findAll();
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("saveItem Tests")
    class SaveItemTests {

        @Test
        @DisplayName("should save item without request ID and return DTO")
        void saveItem_whenNoRequestIdAndUserExists_shouldSaveAndReturnDto() {
            Item itemToSave = new Item();
            itemToSave.setName(newItemDto.getName());
            itemToSave.setDescription(newItemDto.getDescription());
            itemToSave.setAvailable(newItemDto.getAvailable());

            Item savedItem = new Item();
            savedItem.setId(item1Id);
            savedItem.setName(newItemDto.getName());
            savedItem.setDescription(newItemDto.getDescription());
            savedItem.setAvailable(newItemDto.getAvailable());
            savedItem.setOwner(ownerUser);
            savedItem.setRequest(null);

            ItemDto resultDto = new ItemDto(item1Id, newItemDto.getName(),
                    newItemDto.getDescription(), newItemDto.getAvailable());

            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemMapper.mapToItem(newItemDto)).thenReturn(itemToSave);
            when(itemRepository.save(any(Item.class))).thenReturn(savedItem);
            when(itemMapper.mapToDto(savedItem)).thenReturn(resultDto);

            ItemDto result = itemService.saveItem(newItemDto, ownerUserId);

            assertThat("Returned ItemDto should not be null", result, is(notNullValue()));
            assertThat("Returned ItemDto should match the expected DTO", result, equalTo(resultDto));

            verify(userRepository).findById(ownerUserId);
            verify(itemMapper).mapToItem(newItemDto);
            verify(itemRequestRepository, never()).findById(anyLong());
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertThat(
                    "Saved Item should have correct owner and properties from DTO, and null request",
                    capturedItem,
                    allOf(
                            hasProperty("owner", equalTo(ownerUser)),
                            hasProperty("name", equalTo(newItemDto.getName())),
                            hasProperty("description", equalTo(newItemDto.getDescription())),
                            hasProperty("available", equalTo(newItemDto.getAvailable())),
                            hasProperty("request", is(nullValue()))
                    )
            );
            verify(itemMapper).mapToDto(savedItem);
        }

        @Test
        @DisplayName("should save item with valid request ID and return DTO")
        void saveItem_whenValidRequestIdAndUserExists_shouldSaveAndReturnDto() {
            Item itemToSave = new Item();
            itemToSave.setName(newItemDtoWithRequest.getName());
            itemToSave.setDescription(newItemDtoWithRequest.getDescription());
            itemToSave.setAvailable(newItemDtoWithRequest.getAvailable());

            Item savedItem = new Item();
            savedItem.setId(item1Id);
            savedItem.setName(newItemDtoWithRequest.getName());
            savedItem.setDescription(newItemDtoWithRequest.getDescription());
            savedItem.setAvailable(newItemDtoWithRequest.getAvailable());
            savedItem.setOwner(ownerUser);
            savedItem.setRequest(itemRequest1);

            ItemDto resultDto = new ItemDto(item1Id, newItemDtoWithRequest.getName(),
                    newItemDtoWithRequest.getDescription(), newItemDtoWithRequest.getAvailable());

            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemMapper.mapToItem(newItemDtoWithRequest)).thenReturn(itemToSave);
            when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.of(itemRequest1));
            when(itemRepository.save(any(Item.class))).thenReturn(savedItem);
            when(itemMapper.mapToDto(savedItem)).thenReturn(resultDto);

            ItemDto result = itemService.saveItem(newItemDtoWithRequest, ownerUserId);

            assertThat("Returned ItemDto should not be null", result, is(notNullValue()));
            assertThat("Returned ItemDto should match the expected DTO", result,
                    equalTo(resultDto));

            verify(userRepository).findById(ownerUserId);
            verify(itemMapper).mapToItem(newItemDtoWithRequest);
            verify(itemRequestRepository).findById(itemRequestId);
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertThat("Saved Item should have correct owner, request, and properties from DTO",
                    capturedItem,
                    allOf(
                            hasProperty("owner", equalTo(ownerUser)),
                            hasProperty("name", equalTo(newItemDtoWithRequest.getName())),
                            hasProperty("description", equalTo(newItemDtoWithRequest.getDescription())),
                            hasProperty("available", equalTo(newItemDtoWithRequest.getAvailable())),
                            hasProperty("request", equalTo(itemRequest1))
                    )
            );
            verify(itemMapper).mapToDto(savedItem);
        }

        @Test
        @DisplayName("should throw ItemRequestNotFoundException when request ID invalid")
        void saveItem_whenInvalidRequestId_shouldThrowItemRequestNotFoundException() {
            newItemDtoWithRequest.setRequestId(nonExistentRequestId);
            Item itemToSave = new Item();

            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemMapper.mapToItem(newItemDtoWithRequest)).thenReturn(itemToSave);
            when(itemRequestRepository.findById(nonExistentRequestId)).thenReturn(Optional.empty());

            assertThrows(ItemRequestNotFoundException.class,
                    () -> itemService.saveItem(newItemDtoWithRequest, ownerUserId),
                    "Saving item with non-existent request ID should throw "
                            + "ItemRequestNotFoundException");

            verify(userRepository).findById(ownerUserId);
            verify(itemMapper).mapToItem(newItemDtoWithRequest);
            verify(itemRequestRepository).findById(nonExistentRequestId);
            verify(itemRepository, never()).save(any(Item.class));
            verify(itemMapper, never()).mapToDto(any(Item.class));
        }


        @Test
        @DisplayName("should throw NotFoundException when owner not found")
        void saveItem_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.saveItem(newItemDto, ownerUserId),
                    "Saving item when owner user is not found should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verify(itemMapper, never()).mapToItem(any());
            verify(itemRequestRepository, never()).findById(anyLong());
            verify(itemRepository, never()).save(any());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("getItemById Tests")
    class GetItemByIdTests {

        @Test
        @DisplayName("should return ItemDto when item found")
        void getItemById_whenFound_shouldReturnDto() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);

            ItemDto result = itemService.getItemById(item1Id);

            assertThat("Returned ItemDto should not be null", result, is(notNullValue()));
            assertThat("Returned ItemDto should match the expected DTO", result, equalTo(itemDto1));

            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToDto(item1);
        }

        @Test
        @DisplayName("should throw NotFoundException when item not found")
        void getItemById_whenNotFound_shouldThrowItemNotFoundException() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> itemService.getItemById(item1Id),
                    "Getting item by ID when item is not found should throw NotFoundException");

            verify(itemRepository).findById(item1Id);
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("should update item and return DTO when user is owner")
        void update_whenUserIsOwner_shouldUpdateAndReturnDto() {
            Item updatedItem = new Item();
            updatedItem.setId(item1Id);
            updatedItem.setName(updateItemDto.getName());
            updatedItem.setDescription(updateItemDto.getDescription());
            updatedItem.setAvailable(updateItemDto.getAvailable());
            updatedItem.setOwner(ownerUser);

            ItemDto finalDto = new ItemDto(item1Id, updateItemDto.getName(),
                    updateItemDto.getDescription(), updateItemDto.getAvailable());

            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.updateItemFields(updateItemDto, item1)).thenReturn(updatedItem);
            when(itemRepository.save(updatedItem)).thenReturn(updatedItem);
            when(itemMapper.mapToDto(updatedItem)).thenReturn(finalDto);

            ItemDto result = itemService.update(updateItemDto, ownerUserId, item1Id);

            assertThat("Returned ItemDto should not be null", result, is(notNullValue()));
            assertThat("Returned ItemDto should match the expected DTO", result, equalTo(finalDto));

            verify(itemRepository).findById(item1Id);
            verify(itemMapper).updateItemFields(updateItemDto, item1);
            verify(itemRepository).save(itemArgumentCaptor.capture());
            Item capturedItem = itemArgumentCaptor.getValue();
            assertThat(
                    "Saved Item should be the same instance returned by updateItemFields and have "
                            + "updated properties",
                    capturedItem,
                    allOf(
                            is(sameInstance(updatedItem)),
                            hasProperty("id", equalTo(item1Id)),
                            hasProperty("name", equalTo(updateItemDto.getName())),
                            hasProperty("description", equalTo(updateItemDto.getDescription())),
                            hasProperty("available", equalTo(updateItemDto.getAvailable())),
                            hasProperty("owner", equalTo(ownerUser))
                    )
            );
            verify(itemMapper).mapToDto(updatedItem);
        }

        @Test
        @DisplayName("should throw NotFoundException when item not found")
        void update_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.update(updateItemDto, ownerUserId, item1Id),
                    "Updating non-existent item should throw NotFoundException");

            verify(itemRepository).findById(item1Id);
            verify(itemMapper, never()).updateItemFields(any(), any());
            verify(itemRepository, never()).save(any());
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not owner")
        void update_whenUserIsNotOwner_shouldThrowAccessDeniedException() {
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));

            assertThrows(AccessDeniedException.class,
                    () -> itemService.update(updateItemDto, otherUserId, item1Id),
                    "Updating item when user is not the owner should throw AccessDeniedException");

            verify(itemRepository).findById(item1Id);
            verify(itemMapper, never()).updateItemFields(any(), any());
            verify(itemRepository, never()).save(any());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("getItemsByUserId Tests")
    class GetItemsByUserIdTests {

        @Test
        @DisplayName("should return items for existing user")
        void getItemsByUserId_whenUserExists_shouldReturnItems() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(List.of(item1, item2));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);
            when(itemMapper.mapToDto(item2)).thenReturn(itemDto2);

            List<ItemDto> result = itemService.getItemsByUserId(ownerUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should contain 2 items", result, hasSize(2));
            assertThat("Result list should contain the expected ItemDto objects", result,
                    contains(itemDto1, itemDto2));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verify(itemMapper, times(2)).mapToDto(any(Item.class));
        }

        @Test
        @DisplayName("should return empty list for user with no items")
        void getItemsByUserId_whenUserHasNoItems_shouldReturnEmptyList() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(Collections.emptyList());

            List<ItemDto> result = itemService.getItemsByUserId(ownerUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should be empty", result, is(empty()));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should throw NotFoundException when user not found")
        void getItemsByUserId_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.getItemsByUserId(ownerUserId),
                    "Getting items when user is not found should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository, never()).findByOwnerId(anyLong());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete item when user is owner")
        void delete_whenUserIsOwner_shouldDeleteItem() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));

            assertDoesNotThrow(() -> itemService.delete(item1Id, ownerUserId),
                    "Deleting item by owner should not throw exception");

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemRepository).deleteById(item1Id);
        }

        @Test
        @DisplayName("should throw NotFoundException when user not found")
        void delete_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.delete(item1Id, ownerUserId),
                    "Deleting item when user is not found should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository, never()).findById(anyLong());
            verify(itemRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("should throw NotFoundException when item not found")
        void delete_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.delete(item1Id, ownerUserId),
                    "Deleting non-existent item should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not owner")
        void delete_whenUserIsNotOwner_shouldThrowAccessDeniedException() {
            when(userRepository.findById(otherUserId)).thenReturn(
                    Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(
                    Optional.of(item1));

            assertThrows(AccessDeniedException.class,
                    () -> itemService.delete(item1Id, otherUserId),
                    "Deleting item when user is not the owner should throw AccessDeniedException");

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("searchItems Tests")
    class SearchItemsTests {

        @Test
        @DisplayName("should return items matching query")
        void searchItems_whenQueryNotBlankAndUserExists_shouldReturnMatchingItems() {
            String query = "One";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.search(query)).thenReturn(
                    List.of(item1));
            when(itemMapper.mapToDto(item1)).thenReturn(itemDto1);

            List<ItemDto> result = itemService.searchItems(query, otherUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should contain 1 item", result, hasSize(1));
            assertThat("Result list should contain the expected ItemDto object", result,
                    contains(itemDto1));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).search(query);
            verify(itemMapper).mapToDto(item1);
        }

        @Test
        @DisplayName("should return empty list when query matches no items")
        void searchItems_whenQueryMatchesNothing_shouldReturnEmptyList() {
            String query = "NonExistent";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.search(query)).thenReturn(Collections.emptyList());

            List<ItemDto> result = itemService.searchItems(query, otherUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should be empty", result, is(empty()));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).search(query);
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should return empty list when query is blank")
        void searchItems_whenQueryIsBlank_shouldReturnEmptyList() {
            String query = "   ";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

            List<ItemDto> result = itemService.searchItems(query, otherUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should be empty for blank query", result, is(empty()));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository, never()).search(anyString());
            verify(itemMapper, never()).mapToDto(any());
        }

        @Test
        @DisplayName("should throw NotFoundException when user not found")
        void searchItems_whenUserNotFound_shouldThrowUserNotFoundException() {
            String query = "test";
            when(userRepository.findById(otherUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.searchItems(query, otherUserId),
                    "Searching items when user is not found should throw NotFoundException");

            verify(userRepository).findById(otherUserId);
            verify(itemRepository, never()).search(anyString());
            verify(itemMapper, never()).mapToDto(any());
        }
    }

    @Nested
    @DisplayName("saveComment Tests")
    class SaveCommentTests {

        @Test
        @DisplayName("should save comment when user exists, item exists, and user booked item")
        void saveComment_whenValid_shouldSaveAndReturnDto() {
            Comment commentToSave = new Comment();
            commentToSave.setText(newCommentDto.getText());

            Comment savedComment = new Comment();
            savedComment.setId(comment1Id);
            savedComment.setText(newCommentDto.getText());
            savedComment.setItem(item1);
            savedComment.setAuthor(otherUser);
            savedComment.setCreated(LocalDateTime.now());

            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                    List.of(lastBookingDto));
            when(commentMapper.mapToComment(newCommentDto)).thenReturn(commentToSave);
            when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
            when(commentMapper.mapToDto(savedComment)).thenReturn(commentDto1);

            CommentDto result = itemService.saveComment(newCommentDto, item1Id, otherUserId);

            assertThat("Returned CommentDto should not be null", result, is(notNullValue()));
            assertThat("Returned CommentDto should match the expected DTO", result,
                    equalTo(commentDto1));

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class));
            verify(commentMapper).mapToComment(newCommentDto);
            verify(commentRepository).save(commentArgumentCaptor.capture());
            Comment capturedComment = commentArgumentCaptor.getValue();
            assertThat("Saved Comment entity should have correct item, author, and text",
                    capturedComment,
                    allOf(
                            hasProperty("item", equalTo(item1)),
                            hasProperty("author", equalTo(otherUser)),
                            hasProperty("text", equalTo(newCommentDto.getText()))
                    )
            );
            verify(commentMapper).mapToDto(savedComment);
        }

        @Test
        @DisplayName("should throw NotFoundException when user not found")
        void saveComment_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.saveComment(newCommentDto, item1Id, otherUserId),
                    "Saving comment when user is not found should throw NotFoundException");

            verify(userRepository).findById(otherUserId);
            verifyNoInteractions(itemRepository, bookingRepository, commentRepository,
                    commentMapper);
        }

        @Test
        @DisplayName("should throw NotFoundException when item not found")
        void saveComment_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.saveComment(newCommentDto, item1Id, otherUserId),
                    "Saving comment for non-existent item should throw NotFoundException");

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verifyNoInteractions(bookingRepository, commentRepository, commentMapper);
        }

        @Test
        @DisplayName("should throw BookingBadRequestException when user did not book item")
        void saveComment_whenUserDidNotBookItem_shouldThrowBookingBadRequestException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                    Collections.emptyList());

            assertThrows(BookingBadRequestException.class,
                    () -> itemService.saveComment(newCommentDto, item1Id, otherUserId),
                    "Saving comment when user has not booked the item should throw "
                            + "BookingBadRequestException");

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class));
            verifyNoInteractions(commentRepository, commentMapper);
        }

        @Test
        @DisplayName("should throw BookingBadRequestException when user booked a different item")
        void saveComment_whenUserBookedDifferentItem_shouldThrowBookingBadRequestException() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            BookingShortDto bookingDifferentItem = new BookingShortDto(300L, otherUserId, item2Id,
                    LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                    Collections.emptyList());

            assertThrows(BookingBadRequestException.class,
                    () -> itemService.saveComment(newCommentDto, item1Id, otherUserId),
                    "Saving comment when user booked a different item should throw "
                            + "BookingBadRequestException");

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class));
            verifyNoInteractions(commentRepository, commentMapper);
        }
    }

    @Nested
    @DisplayName("getItemByIdWithBookingInfo Tests")
    class GetItemByIdWithBookingInfoTests {

        @Test
        @DisplayName("should return item with booking info when user is owner")
        void getItemByIdWithBookingInfo_whenUserIsOwner_shouldReturnDtoWithBookings() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                    List.of(lastBookingDto));
            when(bookingRepository.findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                    any(LocalDateTime.class))).thenReturn(List.of(nextBookingDto));

            ItemWithBookingInfoDto result = itemService.getItemByIdWithBookingInfo(item1Id,
                    ownerUserId);

            assertThat("Returned ItemWithBookingInfoDto should not be null", result,
                    is(notNullValue()));
            assertThat(
                    "Returned ItemWithBookingInfoDto should have correct ID and populated bookings",
                    result,
                    allOf(
                            hasProperty("id", equalTo(item1Id)),
                            hasProperty("lastBooking", equalTo(lastBookingDto)),
                            hasProperty("nextBooking", equalTo(nextBookingDto))
                    )
            );

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToItemWithBookingInfoDto(item1);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class));
            verify(bookingRepository).findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                    any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should return item with null booking info when user is owner but no "
                + "bookings exist")
        void getItemByIdWithBookingInfo_whenUserIsOwnerAndNoBookings_shouldReturnDtoWithNullBookings() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class))).thenReturn(
                    Collections.emptyList());
            when(bookingRepository.findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                    any(LocalDateTime.class))).thenReturn(Collections.emptyList());

            ItemWithBookingInfoDto result = itemService.getItemByIdWithBookingInfo(item1Id,
                    ownerUserId);

            assertThat("Returned ItemWithBookingInfoDto should not be null", result,
                    is(notNullValue()));
            assertThat("Returned ItemWithBookingInfoDto should have correct ID and null bookings",
                    result,
                    allOf(
                            hasProperty("id", equalTo(item1Id)),
                            hasProperty("lastBooking", is(nullValue())),
                            hasProperty("nextBooking", is(nullValue()))
                    )
            );

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToItemWithBookingInfoDto(item1);
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(
                    eq(List.of(item1Id)), any(LocalDateTime.class));
            verify(bookingRepository).findNextApprovedBookingsShortForItems(eq(List.of(item1Id)),
                    any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should return item with null booking info when user is not owner")
        void getItemByIdWithBookingInfo_whenUserIsNotOwner_shouldReturnDtoWithNullBookings() {
            when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);

            ItemWithBookingInfoDto result = itemService.getItemByIdWithBookingInfo(item1Id,
                    otherUserId);

            assertThat("Returned ItemWithBookingInfoDto should not be null", result,
                    is(notNullValue()));
            assertThat("Returned ItemWithBookingInfoDto should have correct ID and null bookings",
                    result,
                    allOf(
                            hasProperty("id", equalTo(item1Id)),
                            hasProperty("lastBooking", is(nullValue())),
                            hasProperty("nextBooking", is(nullValue()))
                    )
            );

            verify(userRepository).findById(otherUserId);
            verify(itemRepository).findById(item1Id);
            verify(itemMapper).mapToItemWithBookingInfoDto(item1);
            verify(bookingRepository, never()).findPastAndCurrentApprovedBookingsShortForItems(
                    anyList(), any(LocalDateTime.class));
            verify(bookingRepository, never()).findNextApprovedBookingsShortForItems(anyList(),
                    any(LocalDateTime.class));
        }

        @Test
        @DisplayName("should throw NotFoundException when user requesting info not found")
        void getItemByIdWithBookingInfo_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.getItemByIdWithBookingInfo(item1Id, ownerUserId),
                    "Getting item info when user is not found should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verifyNoInteractions(itemRepository, itemMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw NotFoundException when item not found")
        void getItemByIdWithBookingInfo_whenItemNotFound_shouldThrowItemNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findById(item1Id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.getItemByIdWithBookingInfo(item1Id, ownerUserId),
                    "Getting info for non-existent item should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findById(item1Id);
            verifyNoInteractions(itemMapper, bookingRepository);
        }
    }


    @Nested
    @DisplayName("getAllItemsByOwnerWithBookingInfo Tests")
    class GetAllItemsByOwnerWithBookingInfoTests {

        @Test
        @DisplayName("should return items with booking info for owner")
        void getAllItemsByOwner_whenValid_shouldReturnItemsWithBookings() {
            ItemWithBookingInfoDto itemWithBookingInfoDto2 = new ItemWithBookingInfoDto(item2Id,
                    "Item Two", "Desc Two", false, Collections.emptySet(), null, null);

            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(List.of(item1, item2));
            when(itemMapper.mapToItemWithBookingInfoDto(item1)).thenReturn(itemWithBookingInfoDto1);
            when(itemMapper.mapToItemWithBookingInfoDto(item2)).thenReturn(itemWithBookingInfoDto2);
            List<Long> itemIds = List.of(item1Id, item2Id);
            when(bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(eq(itemIds),
                    any(LocalDateTime.class))).thenReturn(List.of(lastBookingDto));
            when(bookingRepository.findNextApprovedBookingsShortForItems(eq(itemIds),
                    any(LocalDateTime.class))).thenReturn(List.of(nextBookingDto));

            List<ItemWithBookingInfoDto> result = itemService.getAllItemsByOwnerWithBookingInfo(
                    ownerUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should contain 2 items", result, hasSize(2));

            ItemWithBookingInfoDto resultItem1 = result.stream()
                    .filter(i -> i.getId().equals(item1Id)).findFirst().orElseThrow();
            assertThat("Item One (available) should have last and next bookings set", resultItem1,
                    allOf(
                            hasProperty("lastBooking", equalTo(lastBookingDto)),
                            hasProperty("nextBooking", equalTo(nextBookingDto))
                    )
            );
            ItemWithBookingInfoDto resultItem2 = result.stream()
                    .filter(i -> i.getId().equals(item2Id)).findFirst().orElseThrow();
            assertThat("Item Two (not available) should have null last and next bookings",
                    resultItem2,
                    allOf(
                            hasProperty("lastBooking", is(nullValue())),
                            hasProperty("nextBooking", is(nullValue()))
                    )
            );

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verify(itemMapper, times(2)).mapToItemWithBookingInfoDto(any(Item.class));
            verify(bookingRepository).findPastAndCurrentApprovedBookingsShortForItems(eq(itemIds),
                    timeArgumentCaptor.capture());
            verify(bookingRepository).findNextApprovedBookingsShortForItems(eq(itemIds),
                    eq(timeArgumentCaptor.getValue()));
        }

        @Test
        @DisplayName("should return empty list when owner has no items")
        void getAllItemsByOwner_whenOwnerHasNoItems_shouldReturnEmptyList() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.of(ownerUser));
            when(itemRepository.findByOwnerId(ownerUserId)).thenReturn(Collections.emptyList());

            List<ItemWithBookingInfoDto> result = itemService.getAllItemsByOwnerWithBookingInfo(
                    ownerUserId);

            assertThat("Result list should not be null", result, is(notNullValue()));
            assertThat("Result list should be empty for owner with no items", result, is(empty()));

            verify(userRepository).findById(ownerUserId);
            verify(itemRepository).findByOwnerId(ownerUserId);
            verifyNoInteractions(itemMapper, bookingRepository);
        }

        @Test
        @DisplayName("should throw NotFoundException when user not found")
        void getAllItemsByOwner_whenUserNotFound_shouldThrowUserNotFoundException() {
            when(userRepository.findById(ownerUserId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> itemService.getAllItemsByOwnerWithBookingInfo(ownerUserId),
                    "Getting all items for non-existent user should throw NotFoundException");

            verify(userRepository).findById(ownerUserId);
            verifyNoInteractions(itemRepository, itemMapper, bookingRepository);
        }
    }
}