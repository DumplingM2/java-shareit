package ru.practicum.shareit.server.item;

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
import ru.practicum.shareit.common.dto.booking.BookingShortDto;
import ru.practicum.shareit.common.dto.item.CommentDto;
import ru.practicum.shareit.common.dto.item.ItemDto;
import ru.practicum.shareit.common.dto.item.ItemWithBookingInfoDto;
import ru.practicum.shareit.common.dto.item.NewCommentDto;
import ru.practicum.shareit.common.dto.item.NewItemDto;
import ru.practicum.shareit.common.dto.item.UpdateItemDto;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.exception.AccessDeniedException;
import ru.practicum.shareit.server.exception.BookingBadRequestException;
import ru.practicum.shareit.server.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.mapper.CommentMapper;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

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
    private final ItemRequestRepository itemRequestRepository;

    private record LastNextBookingPair(BookingShortDto lastBooking, BookingShortDto nextBooking) {}

    private Map<Long, LastNextBookingPair> getLastAndNextBookingsForItems(List<Long> itemIds,
                                                                          LocalDateTime now) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<BookingShortDto> pastAndCurrentBookings =
                bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                        itemIds, now);
        List<BookingShortDto> nextBookings =
                bookingRepository.findNextApprovedBookingsShortForItems(
                        itemIds, now);

        Map<Long, BookingShortDto> lastBookingsMap = pastAndCurrentBookings.stream()
                .collect(Collectors.groupingBy(
                        BookingShortDto::getItemId, Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.isEmpty() ? null : list.getFirst())
                ));

        Map<Long, BookingShortDto> nextBookingsMap = nextBookings.stream()
                .collect(Collectors.groupingBy(
                        BookingShortDto::getItemId, Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.isEmpty() ? null : list.getFirst())
                ));

        Map<Long, LastNextBookingPair> result = new HashMap<>();
        for (Long itemId : itemIds) {
            BookingShortDto last = lastBookingsMap.get(itemId);
            BookingShortDto next = nextBookingsMap.get(itemId);
            result.put(itemId, new LastNextBookingPair(last, next));
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
            throw new NotFoundException("User with id " + userId + " not found");
        }

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();
        Map<Long, LastNextBookingPair> bookingInfoMap = getLastAndNextBookingsForItems(itemIds,
                now);

        log.debug("Fetched {} items with booking info for user with id {}", items.size(), userId);

        return items.stream().map(item -> {
            ItemWithBookingInfoDto dto = itemMapper.mapToItemWithBookingInfoDto(item);
            LastNextBookingPair bookingPair = bookingInfoMap.getOrDefault(item.getId(),
                    new LastNextBookingPair(null, null));
            dto.setLastBooking(bookingPair.lastBooking());
            dto.setNextBooking(bookingPair.nextBooking());
            return dto;
        }).toList();
    }

    @Override
    public ItemDto saveItem(NewItemDto newItemDto, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException(
                    "User with id " + userId + " not found");
        });

        Item item = itemMapper.mapToItem(newItemDto);
        item.setOwner(owner);

        if (newItemDto.getRequestId() != null) {
            log.debug("Linking item to request {}", newItemDto.getRequestId());
            ItemRequest request = itemRequestRepository.findById(newItemDto.getRequestId())
                    .orElseThrow(() -> {
                        log.warn("ItemRequest with id {} not found when saving item",
                                newItemDto.getRequestId());
                        return new ItemRequestNotFoundException(
                                "ItemRequest with id " + newItemDto.getRequestId() + " not found");
                    });
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        log.debug("Saved new item: {}", savedItem);
        return itemMapper.mapToDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        return itemMapper.mapToDto(itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found", id);
            return new NotFoundException(
                    "Item with id " + id + " not found");
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingInfoDto getItemByIdWithBookingInfo(Long itemId, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new NotFoundException(
                    "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found when requested by user {}", itemId, userId);
            return new NotFoundException("Item with id " + itemId + " not found");
        });

        ItemWithBookingInfoDto itemDto = itemMapper.mapToItemWithBookingInfoDto(item);

        if (item.getOwner().getId().equals(userId)) {
            log.debug("User {} is owner of item {}. Fetching booking info.", userId, itemId);
            LocalDateTime now = LocalDateTime.now();
            Map<Long, LastNextBookingPair> bookingInfoMap = getLastAndNextBookingsForItems(
                    Collections.singletonList(itemId), now);
            LastNextBookingPair bookingPair = bookingInfoMap.getOrDefault(itemId,
                    new LastNextBookingPair(null, null));
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
            return new NotFoundException(
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
            throw new NotFoundException(
                    "User with id " + userId + " not found");
        }
        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream()
                .map(itemMapper::mapToDto).toList();
        log.debug("Fetched {} items for user with id {}", items.size(), userId);
        return items;
    }

    @Override
    public void delete(Long id, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("User with id {} not found", userId);
            throw new NotFoundException(
                    "User with id " + userId + " not found");
        }
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found for delete", id);
            return new NotFoundException(
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
            throw new NotFoundException(
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
            return new NotFoundException(
                    "User with id " + userId + " not found");
        });
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(
                    "Item with id " + itemId + " not found");
        });

        List<BookingShortDto> bookings =
                bookingRepository.findPastAndCurrentApprovedBookingsShortForItems(
                        List.of(itemId), LocalDateTime.now());
        boolean userBookedItem = bookings.stream().anyMatch(b -> b.getBookerId().equals(userId));

        if (!userBookedItem) {
            log.warn(
                    "User {} cannot comment on item {} because they haven't completed a booking for "
                            + "it.",
                    userId, itemId);
            throw new BookingBadRequestException(
                    "User " + userId + " cannot comment on item " + itemId
                            + " as they haven't booked it or the booking is not approved/completed.");
        }

        Comment comment = commentMapper.mapToComment(newCommentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        Comment savedComment = commentRepository.save(comment);
        log.debug("Saved new comment: {}", savedComment);
        return commentMapper.mapToDto(savedComment);
    }
}
