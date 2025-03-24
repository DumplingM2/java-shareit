package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.InMemoryUserRepository;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final InMemoryItemRepository itemRepository;
    private final InMemoryUserRepository userRepository; // для проверки, что владелец существует

    public ItemServiceImpl(InMemoryItemRepository itemRepository, InMemoryUserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Item addItem(Long ownerId, Item item) {
        // Проверка, что владелец существует
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Проверка, что поле available не null (если тест ожидает 400)
        if (item.getAvailable() == null) {
            throw new ValidationException("Field 'available' must not be null");
        }

        item.setOwnerId(ownerId);
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long ownerId, Long itemId, Item item) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        // Только владелец может изменить
        if (!existing.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Only owner can update the item");
        }

        if (item.getName() != null) {
            existing.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existing.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existing.setAvailable(item.getAvailable());
        }
        return itemRepository.save(existing);
    }

    @Override
    public Item getItem(Long userId, Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text);
    }
}
