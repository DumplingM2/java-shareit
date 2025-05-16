package ru.practicum.shareit.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
