package ru.practicum.shareit.server.item.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.item.Comment;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.user.User;


@DisplayName("Item Mapper Implementation Tests")
@ExtendWith(MockitoExtension.class)
class ItemMapperImplTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemMapperImpl itemMapper;

    private Item testItem;
    private User testOwner;
    private ItemRequest testItemRequest;

    private final Long ownerId = 1L;
    private final Long itemId = 10L;
    private final Long requestId = 50L;

    @BeforeEach
    void setUp() {
        testOwner = new User();
        testOwner.setId(1L);
        testOwner.setName("Owner Name");
        testOwner.setEmail("owner@example.com");

        testItemRequest = new ItemRequest();
        testItemRequest.setId(requestId);
        testItemRequest.setDescription("Need this item");

        testItem = new Item();
        testItem.setId(10L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setAvailable(true);
        testItem.setOwner(testOwner);
        testItem.setComments(Collections.emptySet());
    }


    @Nested
    @DisplayName("mapToDto Tests")
    class MapToDtoTests {

        @Test
        @DisplayName("mapToDto should map Item to ItemDto correctly")
        void mapToDto_whenItemIsValid_shouldReturnCorrectItemDto() {

            ItemDto itemDto = itemMapper.mapToDto(testItem);

            assertThat("Mapped ItemDto should not be null", itemDto, is(notNullValue()));
            assertThat("Mapped ItemDto should have correct properties", itemDto,
                    allOf(
                            hasProperty("id", equalTo(itemId)),
                            hasProperty("name", equalTo("Test Item")),
                            hasProperty("description", equalTo("Test Description")),
                            hasProperty("available", equalTo(true))
                    )
            );
        }
    }

    @Nested
    @DisplayName("mapToItemWithBookingInfoDto Tests")
    class MapToItemWithBookingInfoDtoTests {

        @Test
        @DisplayName("mapToItemWithBookingInfoDto should map Item with no comments correctly")
        void mapToItemWithBookingInfoDto_whenItemHasNoComments_shouldReturnDtoWithEmptyComments() {
            ItemWithBookingInfoDto resultDto = itemMapper.mapToItemWithBookingInfoDto(testItem);

            assertThat("Mapped ItemWithBookingInfoDto should not be null", resultDto,
                    is(notNullValue()));
            assertThat(
                    "Mapped ItemWithBookingInfoDto should have correct properties and null bookings",
                    resultDto,
                    allOf(
                            hasProperty("id", equalTo(itemId)),
                            hasProperty("name", equalTo("Test Item")),
                            hasProperty("description", equalTo("Test Description")),
                            hasProperty("available", equalTo(true)),
                            hasProperty("lastBooking", is(nullValue())),
                            hasProperty("nextBooking", is(nullValue()))
                    )
            );
            assertThat("Mapped ItemWithBookingInfoDto comments list should be empty",
                    resultDto.getComments(), is(empty()));

            verify(commentMapper, never()).mapToDto(any(Comment.class));
        }

        @Nested
        @DisplayName("mapToItem Tests")
        class MapToItemTests {

            @Test
            @DisplayName("mapToItem should map NewItemDto to Item correctly")
            void mapToItem_whenNewItemDtoIsValid_shouldReturnCorrectItem() {
                NewItemDto newItemDto = new NewItemDto();
                newItemDto.setName("New Item Name");
                newItemDto.setDescription("New Item Description");
                newItemDto.setAvailable(true);
                newItemDto.setRequestId(null);

                Item item = itemMapper.mapToItem(newItemDto);

                assertThat("Mapped Item should not be null", item, is(notNullValue()));
                assertThat("Mapped Item should have correct properties from DTO and default values",
                        item,
                        allOf(
                                hasProperty("id", is(nullValue())),
                                hasProperty("name", equalTo("New Item Name")),
                                hasProperty("description", equalTo("New Item Description")),
                                hasProperty("available", equalTo(true)),
                                hasProperty("owner", is(nullValue())),
                                hasProperty("request", is(nullValue()))
                        )
                );
                assertThat("Mapped Item comments collection should be empty", item.getComments(),
                        is(empty()));
            }
        }

        @Nested
        @DisplayName("updateItemFields Tests")
        class UpdateItemFieldsTests {

            @Test
            @DisplayName("updateItemFields should update all fields when DTO provides all")
            void updateItemFields_whenDtoHasAllFields_shouldUpdateAllFields() {
                UpdateItemDto updateDto = new UpdateItemDto("Updated Name", "Updated Description",
                        false);

                Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

                assertThat("updateItemFields should return the same item instance that was passed",
                        updatedItem, is(sameInstance(testItem)));
                assertThat(
                        "Updated Item should have all fields updated from DTO, except immutable ones",
                        updatedItem,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Updated Name")),
                                hasProperty("description", equalTo("Updated Description")),
                                hasProperty("available", equalTo(false)),
                                hasProperty("owner", sameInstance(testOwner)),
                                hasProperty("request", is(nullValue()))
                        )
                );
            }

            @Test
            @DisplayName("updateItemFields should update only name when DTO provides only name")
            void updateItemFields_whenDtoHasOnlyName_shouldUpdateOnlyName() {
                UpdateItemDto updateDto = new UpdateItemDto("Updated Name", null, null);

                Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

                assertThat("updateItemFields should return the same item instance that was passed",
                        updatedItem, is(sameInstance(testItem)));
                assertThat("Updated Item should have name updated, other fields unchanged",
                        updatedItem,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Updated Name")),
                                hasProperty("description", equalTo("Test Description")),
                                hasProperty("available", equalTo(true))
                        )
                );
            }

            @Test
            @DisplayName("updateItemFields should update only description when DTO provides only "
                    + "description")
            void updateItemFields_whenDtoHasOnlyDescription_shouldUpdateOnlyDescription() {
                UpdateItemDto updateDto = new UpdateItemDto(null, "Updated Description", null);

                Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

                assertThat("updateItemFields should return the same item instance that was passed",
                        updatedItem, is(sameInstance(testItem)));
                assertThat("Updated Item should have description updated, other fields unchanged",
                        updatedItem,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Test Item")),
                                hasProperty("description", equalTo("Updated Description")),
                                hasProperty("available", equalTo(true))
                        )
                );
            }

            @Test
            @DisplayName("updateItemFields should update only availability when DTO provides only "
                    + "availability")
            void updateItemFields_whenDtoHasOnlyAvailability_shouldUpdateOnlyAvailability() {
                UpdateItemDto updateDto = new UpdateItemDto(null, null, false);

                Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

                assertThat("updateItemFields should return the same item instance that was passed",
                        updatedItem, is(sameInstance(testItem)));
                assertThat("Updated Item should have availability updated, other fields unchanged",
                        updatedItem,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Test Item")),
                                hasProperty("description", equalTo("Test Description")),
                                hasProperty("available", equalTo(false))
                        )
                );
            }

            @Test
            @DisplayName("updateItemFields should not update fields when DTO provides null for them")
            void updateItemFields_whenDtoHasNullFields_shouldNotUpdateFields() {
                UpdateItemDto updateDto = new UpdateItemDto(null, null, null);

                Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

                assertThat("updateItemFields should return the same item instance that was passed",
                        updatedItem, is(sameInstance(testItem)));
                assertThat("Updated Item fields should remain unchanged when DTO fields are null",
                        updatedItem,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Test Item")),
                                hasProperty("description", equalTo("Test Description")),
                                hasProperty("available", equalTo(true))
                        )
                );
            }

            @Test
            @DisplayName("updateItemFields should handle empty string updates for name and description")
            void updateItemFields_whenDtoHasEmptyStrings_shouldUpdateFieldsWithEmptyStrings() {
                UpdateItemDto updateDto = new UpdateItemDto("", "", null);

                Item updatedItem = itemMapper.updateItemFields(updateDto, testItem);

                assertThat("updateItemFields should return the same item instance that was passed",
                        updatedItem, is(sameInstance(testItem)));
                assertThat("Updated Item should have name and description updated to empty strings",
                        updatedItem,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("")),
                                hasProperty("description", equalTo("")),
                                hasProperty("available", equalTo(true))
                        )
                );
            }
        }

        @Nested
        @DisplayName("mapToShortDto Tests")
        class MapToShortDtoTests {

            @Test
            @DisplayName("mapToShortDto should map Item with Request correctly")
            void mapToShortDto_whenItemHasRequest_shouldReturnCorrectShortDto() {
                testItem.setRequest(testItemRequest);

                ItemShortDto shortDto = itemMapper.mapToShortDto(testItem);

                assertThat("Mapped ItemShortDto should not be null", shortDto, is(notNullValue()));
                assertThat("Mapped ItemShortDto should have correct properties including requestId",
                        shortDto,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Test Item")),
                                hasProperty("description", equalTo("Test Description")),
                                hasProperty("available", equalTo(true)),
                                hasProperty("ownerId", equalTo(ownerId)),
                                hasProperty("requestId", equalTo(requestId))
                        )
                );
            }

            @Test
            @DisplayName("mapToShortDto should map Item without Request correctly")
            void mapToShortDto_whenItemHasNoRequest_shouldReturnDtoWithNullRequestId() {
                ItemShortDto shortDto = itemMapper.mapToShortDto(testItem);

                assertThat("Mapped ItemShortDto should not be null", shortDto, is(notNullValue()));
                assertThat("Mapped ItemShortDto should have correct properties and null requestId",
                        shortDto,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Test Item")),
                                hasProperty("description", equalTo("Test Description")),
                                hasProperty("available", equalTo(true)),
                                hasProperty("ownerId", equalTo(ownerId)),
                                hasProperty("requestId", is(nullValue()))
                        )
                );
            }

            @Test
            @DisplayName("mapToShortDto should handle null Owner")
            void mapToShortDto_whenOwnerIsNull_shouldReturnDtoWithNullOwnerId() {
                testItem.setOwner(null);

                ItemShortDto shortDto = itemMapper.mapToShortDto(testItem);

                assertThat("Mapped ItemShortDto should not be null", shortDto, is(notNullValue()));
                assertThat("Mapped ItemShortDto should have null ownerId when item owner is null",
                        shortDto,
                        allOf(
                                hasProperty("id", equalTo(itemId)),
                                hasProperty("name", equalTo("Test Item")),
                                hasProperty("description", equalTo("Test Description")),
                                hasProperty("available", equalTo(true)),
                                hasProperty("ownerId", is(nullValue()))
                        )
                );
                assertThat("Mapped ItemShortDto requestId should be null when item request is null",
                        shortDto, hasProperty("requestId", is(nullValue())));
            }
        }
    }
}