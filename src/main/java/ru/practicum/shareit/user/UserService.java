package ru.practicum.shareit.user;

import java.util.List;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

interface UserService {

    List<UserDto> getAllUsers();

    UserDto saveUser(NewUserDto user);

    UserDto getById(Long id);

    UserDto update(UpdateUserDto updatedUser, Long userId);

    void delete(Long id);
}