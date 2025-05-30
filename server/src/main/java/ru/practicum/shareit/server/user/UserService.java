package ru.practicum.shareit.server.user;

import java.util.List;
import ru.practicum.shareit.common.dto.user.NewUserDto;
import ru.practicum.shareit.common.dto.user.UpdateUserDto;
import ru.practicum.shareit.common.dto.user.UserDto;

interface UserService {

    List<UserDto> getAllUsers();

    UserDto saveUser(NewUserDto user);

    UserDto getById(Long id);

    UserDto update(UpdateUserDto updatedUser, Long userId);

    void delete(Long id);
}