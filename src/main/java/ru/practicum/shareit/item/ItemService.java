package ru.practicum.shareit.item;

import java.util.List;

import org.apache.coyote.BadRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

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

    CommentDto saveComment(NewCommentDto comment, Long itemId, Long userId) throws BadRequestException;
}