package ru.practicum.shareit.server.request;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> addRequest(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody NewItemRequestDto dto) {

        log.info("Processing request from user {} to add new item request: {}", userId, dto.getDescription());
        ItemRequestDto savedRequest = itemRequestService.addRequest(dto, userId);
        log.info("Item request added successfully with ID: {}", savedRequest.getId());
        return ResponseEntity
                .created(URI.create("/requests/" + savedRequest.getId()))
                .body(savedRequest);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getOwnRequests(
            @RequestHeader(USER_ID_HEADER) Long userId) {

        log.info("Processing request from user {} to get their own item requests", userId);
        List<ItemRequestDto> requests = itemRequestService.getOwnRequests(userId);
        log.info("Found {} own item requests for user {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(name = "from", required = false) Integer from,
            @RequestParam(name = "size", required = false) Integer size) {

        log.info("Processing request from user {} to get all other requests (from={}, size={})", userId, from, size);
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(userId, from, size);
        log.info("Found {} other item requests page (from={}, size={}) for user {}", requests.size(), from, size, userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequestById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long requestId) {

        log.info("Processing request from user {} to get item request with ID {}", userId, requestId);
        ItemRequestDto request = itemRequestService.getRequestById(requestId, userId);
        log.info("Successfully fetched item request {}", requestId);
        return ResponseEntity.ok(request);
    }
}