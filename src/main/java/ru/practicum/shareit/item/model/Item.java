package ru.practicum.shareit.item.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId; // идентификатор владельца

    public Item() { }
}
