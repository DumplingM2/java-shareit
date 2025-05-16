package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    private record lastNextBookingPair(BookingShortDto lastBooking,
                                       BookingShortDto nextBooking) {
    }

    private Map<Long, lastNextBookingPair> getLastAndNextBookingsForItems(List<Long> itemIds, LocalDateTime now) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<BookingShortDto> pastAndCurrentBookings = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(itemIds, now);
        List<BookingShortDto> nextBookings = bookingRepository.findNextApprovedBookingsShortForItems(itemIds, now);

        Map<Long, BookingShortDto> lastBookingsMap = pastAndCurrentBookings.stream()
                .collect(Collectors.groupingBy(
                        BookingShortDto::getItemId,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.isEmpty() ? null : list.getFirst())
                ));

        Map<Long, BookingShortDto> nextBookingsMap = nextBookings.stream()
                .collect(Collectors.groupingBy(
                        BookingShortDto::getItemId,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.isEmpty() ? null : list.getFirst())
                ));

        Map<Long, lastNextBookingPair> result = new HashMap<>();
        for (Long itemId : itemIds) {
            BookingShortDto last = lastBookingsMap.get(itemId);
            BookingShortDto next = nextBookingsMap.get(itemId);
            result.put(itemId, new lastNextBookingPair(last, next));
        }
        return result;
    }

    @Override
    public List<ItemDto> getAllItems() {
        List<ItemDto> items = itemRepository.findAll().stream().map(itemMapper::mapToDto).toList();
        log.debug("Fetched {} items", items.size());
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemWithBookingInfoDto> getAllItemsByOwnerWithBookingInfo(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException("User with id " + userId + " not found");
        }

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();
        Map<Long, lastNextBookingPair> bookingInfoMap = getLastAndNextBookingsForItems(itemIds,
                now);

        return items.stream().map(item -> {
            ItemWithBookingInfoDto dto = itemMapper.mapToItemWithBookingInfoDto(item);
            lastNextBookingPair bookingPair = bookingInfoMap.getOrDefault(item.getId(),
                    new lastNextBookingPair(null, null));
            dto.setLastBooking(bookingPair.lastBooking());
            dto.setNextBooking(bookingPair.nextBooking());
            return dto;
        }).toList();
    }

    @Override
    public ItemDto saveItem(NewItemDto newItemDto, Long userId) {
        Item item = itemMapper.mapToItem(newItemDto);
        item.setOwner(userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new UserNotFoundException(
                    "User with id " + userId + " not found");
        }));
        Item savedItem = itemRepository.save(item);
        log.debug("Saved new item: {}", savedItem);
        return itemMapper.mapToDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        return itemMapper.mapToDto(itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found", id);
            return new ItemNotFoundException(
                    "Item with id " + id + " not found");
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingInfoDto getItemByIdWithBookingInfo(Long itemId, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                    "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found when requested by user {}", itemId, userId);
            return new ItemNotFoundException("Item with id " + itemId + " not found");
        });

        ItemWithBookingInfoDto itemDto = itemMapper.mapToItemWithBookingInfoDto(item);

        if (item.getOwner().getId().equals(userId)) {
            log.debug("User {} is owner of item {}. Fetching booking info.", userId, itemId);
            LocalDateTime now = LocalDateTime.now();
            Map<Long, lastNextBookingPair> bookingInfoMap = getLastAndNextBookingsForItems(Collections.singletonList(itemId), now);
            lastNextBookingPair bookingPair = bookingInfoMap.getOrDefault(itemId, new lastNextBookingPair(null, null));
            itemDto.setLastBooking(bookingPair.lastBooking());
            itemDto.setNextBooking(bookingPair.nextBooking());
        } else {
            log.debug("User {} is not owner of item {}. Skipping booking info.", userId, itemId);
        }
        return itemDto;
    }

    @Override
    public ItemDto update(UpdateItemDto updateItemDto, Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found for update", itemId);
            return new ItemNotFoundException(
                    "Item with id " + itemId + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, itemId);
            throw new AccessDeniedException(
                    "User with id " + userId + " does not own item with id " + itemId);
        }
        Item updatedItem = itemMapper.updateItemFields(updateItemDto, item);
        itemRepository.save(updatedItem);
        log.debug("Updated item: {}", updatedItem);
        return itemMapper.mapToDto(updatedItem);
    }

    @Override
    public List<ItemDto> getItemsByUserId(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                    "User with id " + userId + " not found");
        }
        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream().map(itemMapper::mapToDto)
                .toList();
        log.debug("Fetched {} items for user with id {}", items.size(), userId);
        return items;
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                    "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found for delete", id);
            return new ItemNotFoundException(
                    "Item with id " + id + " not found");
        });
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("User with id {} does not own item with id {}", userId, id);
            throw new AccessDeniedException(
                    "User with id " + userId + " does not own item with id " + id);
        }
        log.debug("Deleting item with id {} by user with id {}", id, userId);
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> searchItems(String query, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new UserNotFoundException(
                    "User with id " + userId + " not found");
        }
        if (query.isBlank()) {
            log.debug("Search query is blank, returning empty list");
            return List.of();
        }
        // Shouldn't we filter out user's own items when searching?..
        List<ItemDto> items = itemRepository.search(query).stream().map(itemMapper::mapToDto)
                .toList();
        log.debug("Found {} items by query: {}", items.size(), query);
        return items;
    }

    @Override
    public CommentDto saveComment(NewCommentDto newCommentDto, Long itemId, Long userId) {
        User author = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new UserNotFoundException(
                    "User with id " + userId + " not found");
        });
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new ItemNotFoundException(
                    "Item with id " + itemId + " not found");
        });
        List<BookingShortDto> bookings = bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                List.of(itemId), LocalDateTime.now());
        if (bookings.stream().noneMatch(b -> b.getBookerId().equals(userId))) {
            log.warn("User with id {} did not book item with id {}", userId, itemId);
            throw new IllegalArgumentException(
                    "User with id " + userId + " did not book item with id " + itemId);
        }
        Comment comment = commentMapper.mapToComment(newCommentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        Comment savedComment = commentRepository.save(comment);
        log.debug("Saved new comment: {}", savedComment);
        return commentMapper.mapToDto(savedComment);
    }
}