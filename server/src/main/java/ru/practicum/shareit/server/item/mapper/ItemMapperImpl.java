package ru.practicum.shareit.server.item.mapper;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemShortDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.item.Item;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ItemMapperImpl implements ItemMapper {

    private final CommentMapper commentMapper;

    @Override
    public ItemDto mapToDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
    }

    @Override
    public ItemWithBookingInfoDto mapToItemWithBookingInfoDto(Item item) {
        ItemWithBookingInfoDto dto = new ItemWithBookingInfoDto(item.getId(), item.getName(),
                item.getDescription(), item.getAvailable(), null, null, null);

        if (item.getComments() != null) {
            dto.setComments(
                    item.getComments().stream().map(commentMapper::mapToDto).collect(Collectors.toSet()));
        }
        return dto;
    }

    @Override
    public Item mapToItem(NewItemDto newItemDto) {
        Item item = new Item();
        item.setName(newItemDto.getName());
        item.setDescription(newItemDto.getDescription());
        item.setAvailable(newItemDto.getAvailable());
        return item;
    }

    @Override
    public Item updateItemFields(UpdateItemDto updateItemDto, Item item) {
        if (updateItemDto.getName() != null) {
            item.setName(updateItemDto.getName());
        }
        if (updateItemDto.getDescription() != null) {
            item.setDescription(updateItemDto.getDescription());
        }
        if (updateItemDto.getAvailable() != null) {
            item.setAvailable(updateItemDto.getAvailable());
        }
        return item;
    }

    @Override
    public ItemShortDto mapToShortDto(Item item) {
        Long ownerId = (item.getOwner() != null) ? item.getOwner().getId() : null;
        Long requestId = (item.getRequest() != null) ? item.getRequest().getId() : null;

        return new ItemShortDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                ownerId,
                requestId
        );
    }
}