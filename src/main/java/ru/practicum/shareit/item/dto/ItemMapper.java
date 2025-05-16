package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

public interface ItemMapper {

    ItemDto mapToDto(Item item);

    ItemWithBookingInfoDto mapToItemWithBookingInfoDto(Item item);

    Item mapToItem(NewItemDto newItemDto);

    Item updateItemFields(UpdateItemDto updateItemDto, Item item);
}