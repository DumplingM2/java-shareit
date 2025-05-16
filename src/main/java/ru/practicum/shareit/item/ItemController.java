package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                           @Valid @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item created = itemService.addItem(ownerId, item);
        return ResponseEntity.ok(ItemMapper.toDto(created));
    }

    // Редактирование вещи (только владельцем)
    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item updated = itemService.updateItem(ownerId, itemId, item);
        return ResponseEntity.ok(ItemMapper.toDto(updated));
    }

    // Просмотр информации о вещи (любым пользователем)
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable Long itemId) {
        Item item = itemService.getItem(userId, itemId);
        return ResponseEntity.ok(ItemMapper.toDto(item));
    }

    // Получение списка вещей владельца
    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        List<ItemDto> dtos = itemService.getItemsByOwner(ownerId)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Поиск доступных вещей по тексту в названии или описании
    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        List<ItemDto> dtos = itemService.searchItems(text)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
