package ru.practicum.shareit.server.request;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;
import ru.practicum.shareit.server.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDto addRequest(NewItemRequestDto dto, Long userId) {
        log.debug("Attempting to add new item request by user {}", userId);
        User requestor = findUserOrThrow(userId);

        ItemRequest request = itemRequestMapper.mapToEntity(dto);
        request.setRequestor(requestor);

        ItemRequest savedRequest = itemRequestRepository.save(request);
        log.info("Successfully added item request {} by user {}", savedRequest.getId(), userId);
        return itemRequestMapper.mapToDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.debug("Fetching own item requests for user {}", userId);
        findUserOrThrow(userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        log.info("Found {} requests for user {}", requests.size(), userId);
        return requests.stream()
                .map(itemRequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.debug("Fetching all item requests (excluding user {}) with pagination from={}, size={}", userId, from, size);
        findUserOrThrow(userId);

        Pageable pageable = createPageable(from, size);

        List<ItemRequestDto> requests = itemRequestRepository.findAllByRequestorIdNot(userId, pageable)
                .stream()
                .map(itemRequestMapper::mapToDto)
                .collect(Collectors.toList());

        log.info("Found {} requests on page for user {}", requests.size(), userId);
        return requests;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.debug("Fetching item request {} for user {}", requestId, userId);
        findUserOrThrow(userId);

        ItemRequest request = itemRequestRepository.findByIdFetchingItems(requestId)
                .orElseThrow(() -> {
                    log.warn("Item request {} not found", requestId);
                    return new ItemRequestNotFoundException("ItemRequest with id " + requestId + " not found");
                });

        log.info("Successfully fetched item request {}", requestId);
        return itemRequestMapper.mapToDto(request);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException("User with id " + userId + " not found");
        });
    }

    private Pageable createPageable(Integer from, Integer size) {
        Sort defaultSort = Sort.by("created").descending();
        if (from == null || size == null || from < 0 || size <= 0) {
            log.warn("Invalid pagination parameters (from={}, size={}). Defaulting to page 0, size 10.", from, size);
            from = 0;
            size = 10;
        }
        return PageRequest.of(from / size, size, defaultSort);
    }
}