package ru.practicum.shareit.server.request.mapper;

import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.request.ItemRequest;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ItemRequestMapperImpl implements ItemRequestMapper {

    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto mapToDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        if (request.getItems() != null) {
            dto.setItems(request.getItems().stream()
                    .map(itemMapper::mapToShortDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setItems(Collections.emptySet());
        }

        return dto;
    }

    @Override
    public ItemRequest mapToEntity(NewItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        return request;
    }
}