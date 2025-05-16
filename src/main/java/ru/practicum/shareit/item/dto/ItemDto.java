package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemDto {
    private Long id;
    @NotBlank(message = "Item name cannot be empty")
    private String name;
    @NotBlank(message = "Item description cannot be empty")
    private String description;
    @NotNull(message = "Available cannot be null")
    private Boolean available;

    public ItemDto(Long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
