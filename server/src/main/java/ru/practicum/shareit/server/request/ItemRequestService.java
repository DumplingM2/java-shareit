package ru.practicum.shareit.server.request;

import java.util.List;
import ru.practicum.shareit.common.dto.request.ItemRequestDto;
import ru.practicum.shareit.common.dto.request.NewItemRequestDto;

public interface ItemRequestService {

    ItemRequestDto addRequest(NewItemRequestDto dto, Long userId);

    List<ItemRequestDto> getOwnRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long requestId, Long userId);

}