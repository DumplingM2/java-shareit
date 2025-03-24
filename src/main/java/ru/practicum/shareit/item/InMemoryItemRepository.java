package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class InMemoryItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findByOwnerId(Long ownerId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwnerId().equals(ownerId)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<Item> search(String text) {
        String lower = text.toLowerCase();
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (Boolean.TRUE.equals(item.getAvailable())) {
                String name = item.getName() == null ? "" : item.getName().toLowerCase();
                String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
                if (name.contains(lower) || description.contains(lower)) {
                    result.add(item);
                }
            }
        }
        return result;
    }
}
