package ru.practicum.shareit.server.item.mapper;

import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.item.Item;

public interface ItemMapper {

    ItemDto mapToDto(Item item);

    ItemWithBookingInfoDto mapToItemWithBookingInfoDto(Item item);

    Item mapToItem(NewItemDto newItemDto);

    Item updateItemFields(UpdateItemDto updateItemDto, Item item);

    ItemShortDto mapToShortDto(Item item);

}
