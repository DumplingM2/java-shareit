package ru.practicum.shareit.server.request.mapper;

import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.server.request.ItemRequest;

public interface ItemRequestMapper {

    ItemRequestDto mapToDto(ItemRequest request);

    ItemRequest mapToEntity(NewItemRequestDto dto);

}