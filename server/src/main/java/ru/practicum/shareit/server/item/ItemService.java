package ru.practicum.shareit.server.item;

import java.util.List;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;

public interface ItemService {

    List<ItemDto> getAllItems();

    List<ItemWithBookingInfoDto> getAllItemsByOwnerWithBookingInfo(Long ownerId);

    ItemDto saveItem(NewItemDto item, Long userId);

    ItemDto getItemById(Long id);

    ItemWithBookingInfoDto getItemByIdWithBookingInfo(Long itemId, Long userId);

    ItemDto update(UpdateItemDto item, Long userId, Long itemId);

    List<ItemDto> getItemsByUserId(Long userId);

    void delete(Long id, Long userId);

    List<ItemDto> searchItems(String query, Long userId);

    CommentDto saveComment(NewCommentDto comment, Long itemId, Long userId);
}
