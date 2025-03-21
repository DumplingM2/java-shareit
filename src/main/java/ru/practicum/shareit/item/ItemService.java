package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item addItem(Long ownerId, Item item);

    Item updateItem(Long ownerId, Long itemId, Item item);

    Item getItem(Long userId, Long itemId);

    List<Item> getItemsByOwner(Long ownerId);

    List<Item> searchItems(String text);
}
