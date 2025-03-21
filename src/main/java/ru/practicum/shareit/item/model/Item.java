package ru.practicum.shareit.item.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Item {
    // Геттеры и сеттеры
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;

    public Item() {
    }
}
