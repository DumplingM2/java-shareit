package ru.practicum.shareit.server.item;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.exception.AccessDeniedException;
import ru.practicum.shareit.server.exception.ItemNotFoundException;
import ru.practicum.shareit.server.exception.UserNotFoundException;

@WebMvcTest(ItemController.class)
@DisplayName("Item Controller WebMvc Tests")
class ItemControllerTest {

    private final Long ownerUserId = 1L;
    private final Long otherUserId = 2L;
    private final Long item1Id = 10L;
    private final Long item2Id = 11L;
    private final Long nonExistentItemId = 99L;
    private final Long commentId = 100L;
    private final String userIdHeaderName = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ItemService itemService;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private ItemWithBookingInfoDto itemWithBookingInfoDto1;
    private ItemWithBookingInfoDto itemWithBookingInfoDto2;
    private NewItemDto newItemDto;
    private UpdateItemDto updateItemDto;
    private CommentDto commentDto;
    private NewCommentDto newCommentDto;

    @BeforeEach
    void setUp() {
        itemDto1 = new ItemDto(item1Id, "Item One", "Desc One", true);
        itemDto2 = new ItemDto(item2Id, "Item Two", "Desc Two", false);

        itemWithBookingInfoDto1 = new ItemWithBookingInfoDto(item1Id, "Item One", "Desc One", true,
                Collections.emptySet(), null, null);
        itemWithBookingInfoDto2 = new ItemWithBookingInfoDto(item2Id, "Item Two", "Desc Two", false,
                Collections.emptySet(), null, null);

        newItemDto = new NewItemDto();
        newItemDto.setName("New Item");
        newItemDto.setDescription("New Desc");
        newItemDto.setAvailable(true);

        updateItemDto = new UpdateItemDto("Updated Name", "Updated Desc", false);

        newCommentDto = new NewCommentDto();
        newCommentDto.setText("Great item!");

        commentDto = new CommentDto(commentId, newCommentDto.getText(), item1Id, "Commenter Name",
                LocalDateTime.now().toString());
    }

    @Test
    @DisplayName("GET /items - Success (Multiple Items)")
    void getUserItems_whenUserExists_shouldReturnOkAndItemList() throws Exception {
        when(itemService.getAllItemsByOwnerWithBookingInfo(ownerUserId)).thenReturn(
                List.of(itemWithBookingInfoDto1, itemWithBookingInfoDto2));

        mockMvc.perform(get("/items").header(userIdHeaderName, ownerUserId))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(item1Id.intValue())))
                .andExpect(jsonPath("$[1].id", is(item2Id.intValue())));

        verify(itemService).getAllItemsByOwnerWithBookingInfo(ownerUserId);
    }

    @Test
    @DisplayName("GET /items - Success (No Items)")
    void getUserItems_whenUserHasNoItems_shouldReturnOkAndEmptyList() throws Exception {
        when(itemService.getAllItemsByOwnerWithBookingInfo(ownerUserId)).thenReturn(
                Collections.emptyList());

        mockMvc.perform(get("/items").header(userIdHeaderName, ownerUserId))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService).getAllItemsByOwnerWithBookingInfo(ownerUserId);
    }

    @Test
    @DisplayName("GET /items - Failure (User Not Found)")
    void getUserItems_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "User not found";
        when(itemService.getAllItemsByOwnerWithBookingInfo(nonExistentItemId)).thenThrow(
                new UserNotFoundException(errorMsg));

        mockMvc.perform(get("/items").header(userIdHeaderName,
                        nonExistentItemId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).getAllItemsByOwnerWithBookingInfo(nonExistentItemId);
    }

    @Test
    @DisplayName("GET /items/{id} - Success")
    void getById_whenItemExists_shouldReturnOkAndItemDto() throws Exception {
        when(itemService.getItemByIdWithBookingInfo(item1Id, ownerUserId)).thenReturn(
                itemWithBookingInfoDto1);

        mockMvc.perform(get("/items/{id}", item1Id).header(userIdHeaderName, ownerUserId))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(item1Id.intValue())))
                .andExpect(jsonPath("$.name", is(itemWithBookingInfoDto1.getName())));

        verify(itemService).getItemByIdWithBookingInfo(item1Id, ownerUserId);
    }

    @Test
    @DisplayName("GET /items/{id} - Failure (Item Not Found)")
    void getById_whenItemNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Item not found";
        when(itemService.getItemByIdWithBookingInfo(nonExistentItemId, ownerUserId)).thenThrow(
                new ItemNotFoundException(errorMsg));

        mockMvc.perform(get("/items/{id}", nonExistentItemId).header(userIdHeaderName, ownerUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).getItemByIdWithBookingInfo(nonExistentItemId, ownerUserId);
    }

    @Test
    @DisplayName("GET /items/{id} - Failure (Requesting User Not Found)")
    void getById_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Requesting user not found";
        when(itemService.getItemByIdWithBookingInfo(item1Id, nonExistentItemId)).thenThrow(
                new UserNotFoundException(errorMsg));

        mockMvc.perform(get("/items/{id}", item1Id).header(userIdHeaderName,
                        nonExistentItemId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).getItemByIdWithBookingInfo(item1Id, nonExistentItemId);
    }

    @Test
    @DisplayName("POST /items - Success")
    void saveItem_whenValid_shouldReturnCreatedAndItemDto() throws Exception {
        when(itemService.saveItem(any(NewItemDto.class), eq(ownerUserId))).thenReturn(itemDto1);

        mockMvc.perform(post("/items").header(userIdHeaderName, ownerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemDto))).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/items/" + item1Id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(item1Id.intValue())))
                .andExpect(jsonPath("$.name", is(itemDto1.getName())));

        verify(itemService).saveItem(refEq(newItemDto), eq(ownerUserId));
    }

    @Test
    @DisplayName("POST /items - Failure (Owner Not Found)")
    void saveItem_whenOwnerNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Owner user not found";
        when(itemService.saveItem(any(NewItemDto.class), eq(nonExistentItemId))).thenThrow(
                new UserNotFoundException(errorMsg));

        mockMvc.perform(post("/items").header(userIdHeaderName, nonExistentItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemDto))).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).saveItem(refEq(newItemDto), eq(nonExistentItemId));
    }

    @Test
    @DisplayName("PATCH /items/{id} - Success")
    void update_whenValid_shouldReturnOkAndItemDto() throws Exception {
        ItemDto updatedResultDto = new ItemDto(item1Id, updateItemDto.getName(),
                updateItemDto.getDescription(), updateItemDto.getAvailable());
        when(itemService.update(any(UpdateItemDto.class), eq(ownerUserId), eq(item1Id))).thenReturn(
                updatedResultDto);

        mockMvc.perform(patch("/items/{id}", item1Id).header(userIdHeaderName, ownerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(item1Id.intValue())))
                .andExpect(jsonPath("$.name", is(updateItemDto.getName())))
                .andExpect(jsonPath("$.available", is(updateItemDto.getAvailable())));

        verify(itemService).update(refEq(updateItemDto), eq(ownerUserId), eq(item1Id));
    }

    @Test
    @DisplayName("PATCH /items/{id} - Failure (Item Not Found)")
    void update_whenItemNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Item to update not found";
        when(itemService.update(any(UpdateItemDto.class), eq(ownerUserId),
                eq(nonExistentItemId))).thenThrow(new ItemNotFoundException(errorMsg));

        mockMvc.perform(
                        patch("/items/{id}", nonExistentItemId).header(userIdHeaderName, ownerUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).update(refEq(updateItemDto), eq(ownerUserId), eq(nonExistentItemId));
    }

    @Test
    @DisplayName("PATCH /items/{id} - Failure (User Not Owner)")
    void update_whenUserNotOwner_shouldReturnForbidden() throws Exception {
        String errorMsg = "User does not own this item";
        when(itemService.update(any(UpdateItemDto.class), eq(otherUserId), eq(item1Id))).thenThrow(
                new AccessDeniedException(errorMsg));

        mockMvc.perform(
                        patch("/items/{id}", item1Id).header(userIdHeaderName, otherUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(403)));

        verify(itemService).update(refEq(updateItemDto), eq(otherUserId), eq(item1Id));
    }

    @Test
    @DisplayName("GET /items/search - Success")
    void searchItems_whenQueryProvided_shouldReturnOkAndItemList() throws Exception {
        String query = "search text";
        when(itemService.searchItems(eq(query), eq(ownerUserId))).thenReturn(List.of(itemDto1));

        mockMvc.perform(
                        get("/items/search").header(userIdHeaderName, ownerUserId).param("text", query))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(item1Id.intValue())));

        verify(itemService).searchItems(eq(query), eq(ownerUserId));
    }

    @Test
    @DisplayName("GET /items/search - Success (Blank Query)")
    void searchItems_whenQueryBlank_shouldReturnOkAndEmptyList() throws Exception {
        String query = "";
        when(itemService.searchItems(eq(query), eq(ownerUserId))).thenReturn(
                Collections.emptyList());

        mockMvc.perform(
                        get("/items/search").header(userIdHeaderName, ownerUserId).param("text", query))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService).searchItems(eq(query), eq(ownerUserId));
    }

    @Test
    @DisplayName("GET /items/search - Failure (User Not Found)")
    void searchItems_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String query = "search text";
        String errorMsg = "Search user not found";
        when(itemService.searchItems(anyString(), eq(nonExistentItemId))).thenThrow(
                new UserNotFoundException(errorMsg));

        mockMvc.perform(
                        get("/items/search").header(userIdHeaderName, nonExistentItemId).param("text",
                                query))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).searchItems(eq(query), eq(nonExistentItemId));
    }

    @Test
    @DisplayName("DELETE /items/{id} - Success")
    void delete_whenValid_shouldReturnNoContent() throws Exception {
        doNothing().when(itemService).delete(eq(item1Id), eq(ownerUserId));

        mockMvc.perform(delete("/items/{id}", item1Id)
                .header(userIdHeaderName, ownerUserId)).andExpect(status().isNoContent());

        verify(itemService).delete(item1Id, ownerUserId);
    }

    @Test
    @DisplayName("DELETE /items/{id} - Failure (Item Not Found)")
    void delete_whenItemNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Item to delete not found";
        doThrow(new ItemNotFoundException(errorMsg)).when(itemService)
                .delete(eq(nonExistentItemId), eq(ownerUserId));

        mockMvc.perform(
                        delete("/items/{id}", nonExistentItemId)
                                .header(userIdHeaderName, ownerUserId)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).delete(nonExistentItemId, ownerUserId);
    }

    @Test
    @DisplayName("DELETE /items/{id} - Failure (User Not Owner)")
    void delete_whenUserNotOwner_shouldReturnForbidden() throws Exception {
        String errorMsg = "Cannot delete item not owned by user";
        doThrow(new AccessDeniedException(errorMsg)).when(itemService)
                .delete(eq(item1Id), eq(otherUserId));

        mockMvc.perform(delete("/items/{id}", item1Id)
                        .header(userIdHeaderName, otherUserId))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(403)));

        verify(itemService).delete(item1Id, otherUserId);
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - Success")
    void saveComment_whenValid_shouldReturnCreatedAndCommentDto() throws Exception {
        when(itemService.saveComment(any(NewCommentDto.class), eq(item1Id),
                eq(otherUserId))).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", item1Id).header(userIdHeaderName,
                                otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(commentId.intValue())))
                .andExpect(jsonPath("$.text", is(newCommentDto.getText())));

        verify(itemService).saveComment(refEq(newCommentDto), eq(item1Id), eq(otherUserId));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - Failure (Item Not Found)")
    void saveComment_whenItemNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Item to comment on not found";
        when(itemService.saveComment(any(NewCommentDto.class), eq(nonExistentItemId),
                eq(otherUserId))).thenThrow(new ItemNotFoundException(errorMsg));

        mockMvc.perform(
                        post("/items/{itemId}/comment", nonExistentItemId).header(userIdHeaderName,
                                        otherUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).saveComment(refEq(newCommentDto), eq(nonExistentItemId),
                eq(otherUserId));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - Failure (User Not Found)")
    void saveComment_whenUserNotFound_shouldReturnNotFound() throws Exception {
        String errorMsg = "Commenting user not found";
        when(itemService.saveComment(any(NewCommentDto.class), eq(item1Id),
                eq(nonExistentItemId))).thenThrow(new UserNotFoundException(errorMsg));

        mockMvc.perform(post("/items/{itemId}/comment", item1Id).header(userIdHeaderName,
                                nonExistentItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is(errorMsg)))
                .andExpect(jsonPath("$.responseCode", is(404)));

        verify(itemService).saveComment(refEq(newCommentDto), eq(item1Id), eq(nonExistentItemId));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - Failure (User Did Not Book)")
    void saveComment_whenUserDidNotBook_shouldReturnBadRequest() throws Exception {
        String errorMsg = "User cannot comment without booking";
        when(itemService.saveComment(any(NewCommentDto.class), eq(item1Id),
                eq(otherUserId))).thenThrow(
                new AccessDeniedException(errorMsg));

        mockMvc.perform(
                        post("/items/{itemId}/comment", item1Id).header(userIdHeaderName, otherUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isForbidden());

        verify(itemService).saveComment(refEq(newCommentDto), eq(item1Id), eq(otherUserId));
    }
}