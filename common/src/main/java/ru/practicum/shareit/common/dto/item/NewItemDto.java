package ru.practicum.shareit.common.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewItemDto {

    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    @NotNull(message = "Item status must be set")
    private Boolean available;
    private Long requestId;
}