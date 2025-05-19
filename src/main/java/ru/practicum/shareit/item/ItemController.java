package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
@SuppressWarnings("unused")
public class ItemController {

    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<List<ItemWithBookingInfoDto>> getUserItems(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Processing request to fetch items for user with ID: {}", userId);
        return ResponseEntity.ok(itemService.getAllItemsByOwnerWithBookingInfo(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemWithBookingInfoDto> getById(
            @RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long id) {
        log.info("Processing request to fetch item by ID: {}", id);
        return ResponseEntity.ok(itemService.getItemByIdWithBookingInfo(id, userId));
    }

    @PostMapping
    public ResponseEntity<ItemDto> saveItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestBody @Valid NewItemDto newItemDto) {
        log.info("Processing request to save a new item...");
        ItemDto savedItem = itemService.saveItem(newItemDto, userId);
        return ResponseEntity.created(java.net.URI.create("/items/" + savedItem.getId()))
                .body(savedItem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long id, @RequestBody @Valid UpdateItemDto updatedItemDto) {
        log.info("Processing request to update item with ID: {}", id);
        return ResponseEntity.ok(itemService.update(updatedItemDto, userId, id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestHeader(USER_ID_HEADER) Long userId,
                                                     @RequestParam String text) {
        log.info("Processing request to search items by query: {}", text);
        return ResponseEntity.ok(itemService.searchItems(text, userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestHeader(USER_ID_HEADER) Long userId,
                                       @RequestParam Long id) {
        log.info("Processing request to delete item with ID: {}", id);
        itemService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<CommentDto> saveComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @PathVariable Long itemId, @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Processing request to save a comment for item with ID: {}", itemId);
        try {
            return ResponseEntity.created(java.net.URI.create("/items/" + itemId + "/comment"))
                    .body(itemService.saveComment(newCommentDto, itemId, userId));
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }
    }
}