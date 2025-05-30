package ru.practicum.shareit.server.request.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.user.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemRequest Mapper Implementation Tests")
class ItemRequestMapperImplTest {

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestMapperImpl itemRequestMapper;

    private ItemRequest itemRequest1;
    private Item itemResponse1;
    private Item itemResponse2;
    private User requestor;
    private ItemShortDto itemShortDto1;
    private ItemShortDto itemShortDto2;
    private NewItemRequestDto newItemRequestDto;
    private LocalDateTime created;

    private final Long requestorId = 1L;
    private final Long requestId = 10L;
    private final Long itemResponse1Id = 100L;
    private final Long itemResponse2Id = 101L;


    @BeforeEach
    void setUp() {
        created = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        requestor = new User();
        requestor.setId(requestorId);
        requestor.setName("Requestor");
        requestor.setEmail("req@example.com");

        User itemOwner = new User(); itemOwner.setId(2L);
        itemResponse1 = new Item();
        itemResponse1.setId(itemResponse1Id);
        itemResponse1.setName("Response Item 1");
        itemResponse1.setDescription("Desc 1");
        itemResponse1.setAvailable(true);
        itemResponse1.setOwner(itemOwner);

        itemResponse2 = new Item();
        itemResponse2.setId(itemResponse2Id);
        itemResponse2.setName("Response Item 2");
        itemResponse2.setDescription("Desc 2");
        itemResponse2.setAvailable(true);
        itemResponse2.setOwner(itemOwner);

        itemRequest1 = new ItemRequest();
        itemRequest1.setId(requestId);
        itemRequest1.setDescription("Need tools");
        itemRequest1.setRequestor(requestor);
        itemRequest1.setCreated(created);
        itemRequest1.setItems(Set.of(itemResponse1, itemResponse2));

        itemResponse1.setRequest(itemRequest1);
        itemResponse2.setRequest(itemRequest1);

        itemShortDto1 = new ItemShortDto(itemResponse1Id, "Response Item 1", "Desc 1", true, 2L,
                requestId);
        itemShortDto2 = new ItemShortDto(itemResponse2Id, "Response Item 2", "Desc 2", true, 2L,
                requestId);

        newItemRequestDto = new NewItemRequestDto("I need a ladder please.");
    }

    @Nested
    @DisplayName("mapToDto Tests")
    class MapToDtoTests {

        @Test
        @DisplayName("mapToDto should map ItemRequest with items correctly")
        void mapToDto_whenRequestHasItems_shouldReturnDtoWithItemShortDtos() {
            when(itemMapper.mapToShortDto(itemResponse1)).thenReturn(itemShortDto1);
            when(itemMapper.mapToShortDto(itemResponse2)).thenReturn(itemShortDto2);

            ItemRequestDto resultDto = itemRequestMapper.mapToDto(itemRequest1);

            assertThat("Mapped ItemRequestDto should not be null", resultDto, is(notNullValue()));
            assertThat("Mapped ItemRequestDto should have correct core properties", resultDto,
                    allOf(
                            hasProperty("id", equalTo(requestId)),
                            hasProperty("description", equalTo("Need tools")),
                            hasProperty("created", equalTo(created))
                    )
            );
            assertThat("Mapped ItemRequestDto items list should not be null", resultDto.getItems(),
                    is(notNullValue()));
            assertThat("Mapped ItemRequestDto items list should have the correct size",
                    resultDto.getItems(), hasSize(2));
            assertThat("Mapped ItemRequestDto items list should contain the correct items",
                    resultDto.getItems(), containsInAnyOrder(itemShortDto1, itemShortDto2));

            verify(itemMapper, times(1)).mapToShortDto(itemResponse1);
            verify(itemMapper, times(1)).mapToShortDto(itemResponse2);
        }

        @Test
        @DisplayName("mapToDto should map ItemRequest with empty items correctly")
        void mapToDto_whenRequestHasEmptyItems_shouldReturnDtoWithEmptyItemSet() {
            itemRequest1.setItems(Collections.emptySet());

            ItemRequestDto resultDto = itemRequestMapper.mapToDto(itemRequest1);

            assertThat("Mapped ItemRequestDto should not be null", resultDto, is(notNullValue()));
            assertThat("Mapped ItemRequestDto should have correct core properties", resultDto,
                    allOf(
                            hasProperty("id", equalTo(requestId)),
                            hasProperty("description", equalTo("Need tools")),
                            hasProperty("created", equalTo(created))
                    )
            );
            assertThat("Mapped ItemRequestDto items list should not be null", resultDto.getItems(),
                    is(notNullValue()));
            assertThat("Mapped ItemRequestDto items list should be empty", resultDto.getItems(),
                    is(empty()));

            verify(itemMapper, never()).mapToShortDto(any());
        }

        @Test
        @DisplayName("mapToDto should map ItemRequest with null items correctly")
        void mapToDto_whenRequestHasNullItems_shouldReturnDtoWithEmptyItemSet() {
            itemRequest1.setItems(null);

            ItemRequestDto resultDto = itemRequestMapper.mapToDto(itemRequest1);

            assertThat("Mapped ItemRequestDto should not be null", resultDto, is(notNullValue()));
            assertThat("Mapped ItemRequestDto should have core properties mapped", resultDto,
                    allOf(
                            hasProperty("id", equalTo(requestId)),
                            hasProperty("description", equalTo("Need tools")),
                            hasProperty("created", equalTo(created))
                    )
            );
            assertThat(
                    "Mapped ItemRequestDto items list should not be null (mapper initializes empty "
                            + "set)",
                    resultDto.getItems(), is(notNullValue()));
            assertThat("Mapped ItemRequestDto items list should be empty", resultDto.getItems(),
                    is(empty()));

            verify(itemMapper, never()).mapToShortDto(any());
        }

        @Test
        @DisplayName("mapToDto should return null when input request is null")
        void mapToDto_whenRequestIsNull_shouldReturnNull() {
            ItemRequestDto resultDto = itemRequestMapper.mapToDto(null);

            assertThat("Mapped ItemRequestDto should be null for null input", resultDto,
                    is(nullValue()));
            verify(itemMapper, never()).mapToShortDto(any());
        }
    }

    @Nested
    @DisplayName("mapToEntity Tests")
    class MapToEntityTests {

        @Test
        @DisplayName("mapToEntity should map description correctly")
        void mapToEntity_whenValidDto_shouldSetDescription() {
            ItemRequest resultEntity = itemRequestMapper.mapToEntity(newItemRequestDto);

            assertThat("Mapped ItemRequest entity should not be null", resultEntity,
                    is(notNullValue()));
            assertThat(
                    "Mapped ItemRequest entity should have correct description and null other "
                            + "properties",
                    resultEntity,
                    allOf(
                            hasProperty("description", equalTo(newItemRequestDto.getDescription())),
                            hasProperty("id", is(nullValue())),
                            hasProperty("requestor", is(nullValue())),
                            hasProperty("created", is(nullValue()))
                    )
            );
            assertThat("Mapped ItemRequest entity items collection should be empty",
                    resultEntity.getItems(), empty());
        }

        @Test
        @DisplayName("mapToEntity should return null when input DTO is null")
        void mapToEntity_whenDtoIsNull_shouldReturnNull() {
            ItemRequest resultEntity = itemRequestMapper.mapToEntity(null);

            assertThat("Mapped ItemRequest entity should be null for null input", resultEntity,
                    is(nullValue()));
        }
    }
}