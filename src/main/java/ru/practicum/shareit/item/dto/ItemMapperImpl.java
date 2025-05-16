package ru.practicum.shareit.item.dto;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ItemMapperImpl implements ItemMapper {

    private final CommentMapper commentMapper;

    @Override
    public ItemDto mapToDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getStatus());
    }

    @Override
    public ItemWithBookingInfoDto mapToItemWithBookingInfoDto(Item item) {
        ItemWithBookingInfoDto dto = new ItemWithBookingInfoDto(item.getId(), item.getName(),
                item.getDescription(), item.getStatus(), null, null, null);
        dto.setComments(
                item.getComments().stream().map(commentMapper::mapToDto).collect(Collectors.toSet()));
        return dto;
    }

    @Override
    public Item mapToItem(NewItemDto newItemDto) {
        Item item = new Item();
        item.setName(newItemDto.getName());
        item.setDescription(newItemDto.getDescription());
        item.setStatus(newItemDto.getStatus());
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
        if (updateItemDto.getStatus() != null) {
            item.setStatus(updateItemDto.getStatus());
        }
        return item;
    }
}